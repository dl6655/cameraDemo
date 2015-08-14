package com.example.cameraDemo.inject;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * @author cuijie
 * 实现对view字段在声明的时候注解形式的findviewById
 */
public class Injector {

    private static final Class<?>[] HALT_CLASSES = { Activity.class, Fragment.class, View.class, Object.class,android.app.Fragment.class };

    public static void inject(Object container) {
        inject(container, container, false);
    }

    public static void inject(Object container, boolean ignore) {
        inject(container, container, ignore);
    }

    public static void inject(Object container, Object parent) {
        inject(container, parent, false);
    }
    public static boolean contains(Object[] array, Object value) {
        if (array == null) {
            return false;
        }
        for (Object object : array) {
            if (object == null) {
                if (value == null) {
                    return true;
                }
            } else if (object.equals(value)) {
                return true;
            }
        }
        return false;
    }
    public static void inject(Object container, Object parent, boolean ignore) {
        Class<?> clazz = container.getClass();
        while (!contains(HALT_CLASSES, clazz)) {
            Field[] fields = clazz.getDeclaredFields();
            clazz = clazz.getSuperclass();
            for (Field field : fields) {
                if (field.isAnnotationPresent(From.class)) {
                    From from = field.getAnnotation(From.class);
                    try {
                        int id = from.value();
                        field.setAccessible(true);
                        View view = null;
                        if (parent instanceof Activity) {
                            view = ((Activity) parent).findViewById(id);
                        } else if (parent instanceof Fragment) {
                            view = ((Fragment) parent).getView().findViewById(id);
						} else if (parent instanceof android.app.Fragment) {
							view = ((android.app.Fragment)parent).getView().findViewById(id);
						}else if (parent instanceof View) {
                            view = ((View) parent).findViewById(id);
                        }

                        if (!ignore && !from.canBeNull() && view == null) {
                            throw new InjectException("field '"
                                    + "' not wired! Check your settings or layout xml id value!");
                        } else if (view != null) {

                            field.set(container, view);
                        }
                    } catch (Exception e) {
                        throw new InjectException(e.getMessage(), e);
                    }
                }
                if (field.isAnnotationPresent(FromArray.class)) {
                    FromArray fromArray = field.getAnnotation(FromArray.class);
                    try {
                        int[] ids = fromArray.value();
                        field.setAccessible(true);
                        Class<?> componentType = field.getType().getComponentType();
                        if (componentType != null) {
                            Object grown = Array.newInstance(componentType, ids.length);
                            ArrayList<View> tempArray = new ArrayList<View>(ids.length);
                            View view;
                            for (int id : ids) {
                                view = null;
                                if (parent instanceof Activity) {
                                    view = ((Activity) parent).findViewById(id);
                                } else if (parent instanceof Fragment) {
                                    view = ((Fragment) parent).getView().findViewById(id);
                                } else if (parent instanceof View) {
                                    view = ((View) parent).findViewById(id);
								} else if (parent instanceof android.app.Fragment) {
									view = ((android.app.Fragment)parent).getView().findViewById(id);
								}
                                if (!ignore && !fromArray.canBeNull() && view == null) {
                                    throw new InjectException("field '"
                                            + "' not wired! Check your settings or layout xml id value!");
                                }
                                tempArray.add(view);
                            }
                            System.arraycopy(tempArray.toArray(), 0, grown, 0, ids.length);
                            field.set(container, grown);
                        }
                    } catch (Exception e) {
                        throw new InjectException(e.getMessage(), e);
                    }
                }
            }

        }
    }

}
