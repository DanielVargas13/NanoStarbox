/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.javascript;

import box.star.js.android.DexFileClassLoader;
import box.star.js.android.Android;

/**
 * Load generated classes.

 * @author Norris Boyd

 * Android Support:

 * @author Triston-Jerard: Taylor

 */
public class DefiningClassLoader extends ClassLoader
    implements GeneratedClassLoader
{

    private static DexFileClassLoader dexLoader;

    public DefiningClassLoader() {
        this(DefiningClassLoader.class.getClassLoader());

    }

    public DefiningClassLoader(ClassLoader parentLoader) {
        if (Android.isPlatform() && dexLoader == null){
            dexLoader = new DexFileClassLoader(parentLoader);
        }
        this.parentLoader = parentLoader;
    }

    public Class<?> defineClass(String name, byte[] data) {
        // Use our own protection domain for the generated classes.
        // TODO: we might want to use a separate protection domain for classes
        // compiled from scripts, based on where the script was loaded from.
        if (dexLoader != null){
            return dexLoader.defineClass(name, data);
        }
        return super.defineClass(name, data, 0, data.length,
                SecurityUtilities.getProtectionDomain(getClass()));
    }

    public void linkClass(Class<?> cl) {
        if (dexLoader != null){
            return;
        }
        resolveClass(cl);
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve)
        throws ClassNotFoundException
    {

        if (dexLoader != null) {
            return dexLoader.loadClass(name, resolve);
        }

        Class<?> cl = findLoadedClass(name);
        if (cl == null) {
            if (parentLoader != null) {
                cl = parentLoader.loadClass(name);
            } else {
                cl = findSystemClass(name);
            }
        }
        if (resolve) {
            resolveClass(cl);
        }
        return cl;
    }

    private final ClassLoader parentLoader;

}
