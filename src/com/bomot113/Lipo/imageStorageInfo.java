package com.bomot113.Lipo;

import java.lang.reflect.Field;

import com.bomot113.Lipo.associations.ImageField;

public class imageStorageInfo {
	private static Field imageField;
	static Field getImageFieldForClass(Class<? extends ImageStorage> klass) {
		if (imageField !=null) return imageField;
		try {
			Field[] fields = klass.getDeclaredFields(); 
			if (fields.length == 0) return null;
			for (int i = 0; i< fields.length; i++){
				if (fields[i].isAnnotationPresent(ImageField.class) && 
						fields[i].getType().isArray()){
					imageField = fields[i];
					imageField.setAccessible(true);
					break;
				}
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return imageField;
	}
}
