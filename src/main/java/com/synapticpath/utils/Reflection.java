/* Copyright (C) 2016 synapticpath.com - All Rights Reserved

 This file is part of Pi-Secure.

    Pi-Secure is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Pi-Secure is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Pi-Secure.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.synapticpath.utils;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.synapticpath.pisecure.App;

/**
 * Provides utility methods for scanning a package in classpath for classes.
 * 
 * @author jmarvan@synapticpath.com
 *
 */
public class Reflection {

	public static File getRootFile() {
		return new File(App.class.getProtectionDomain().getCodeSource().getLocation().getPath());
	}
	
	
	public static <T> Set<Class<T>> getTypesWithInterface(String pkg, final Class<T> interfaceClass) {

		
		try {
			Set<String> classNames = findClassesInPackage(pkg, true);					
			Set<Class<T>> filtered = filterClasses(classNames, cls -> interfaceClass.isAssignableFrom(cls));
			
			return filtered;
		} catch (Exception e) {

		}
		return null;
	}

	
	@SuppressWarnings({"rawtypes","unchecked"})
	public static Set<Class<Object>> getTypesWithAnnotation(String pkg, final Class classAnnotation) {

		try {
			Set<String> classNames = findClassesInPackage(pkg, true);					
			Set<Class<Object>> filtered = filterClasses(classNames, cls -> cls.getAnnotation(classAnnotation) != null);
			
			return filtered;
		} catch (Exception e) {

		}
		return null;
	}
	
	
	
	@SuppressWarnings("unchecked")
	private static <T> Set<Class<T>> filterClasses(Set<String> classNames, Predicate<Class<T>> filter) {
		
		Set<Class<T>> classes = new HashSet<>();
		classNames.forEach(className -> {
			try {
				Class<T> cls = (Class<T>)Class.forName(className);				
				if (filter.test(cls)) {
					classes.add(cls);
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
			
		});
		
		return classes;
	}
	

	private static Set<String> findClassesInPackage(String packageName, boolean recurse) throws IOException {

		Set<String> result = new HashSet<>();
		String packagePath = packageName.replace(".", "/");
		File root = getRootFile();

		if (root.getName().endsWith(".jar")) {

			JarFile jf = new JarFile(root);
			Enumeration<JarEntry> jarEntries = jf.entries();

			while (jarEntries.hasMoreElements()) {

				JarEntry entry = jarEntries.nextElement();
				String entryName = entry.getName();
				if (entryName.startsWith(packagePath)) {

					String filePath = entryName.substring(packagePath.length() + 1);
					if ((!recurse && filePath.indexOf('/') < 0 || recurse) && filePath.endsWith(".class")) {

						result.add(packageName + "." + stripClassExtension(filePath.replace("/", ".")));
					}

				}
			}
			jf.close();

		} else {

			File packageFolder = new File(root, packagePath);
			findClassesInFolder(packageFolder, packageName, result, recurse);

		}

		return result;
	}

	private static void findClassesInFolder(File folder, String pkg, Set<String> result, boolean recurse) {

		for (File contentFile : folder.listFiles()) {
			if (contentFile.isDirectory() && recurse) {
				findClassesInFolder(contentFile, pkg + "." + contentFile.getName(), result, recurse);
			} else if (contentFile.getName().endsWith(".class")) {
				result.add(pkg + "." + stripClassExtension(contentFile.getName()));
			}
		}
	}
	
	private static String stripClassExtension(String fileName) {
		return fileName.substring(0, fileName.length()-6);
	}
}
