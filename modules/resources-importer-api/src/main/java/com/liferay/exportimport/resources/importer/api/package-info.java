/**
 * package com.liferay.exportimport.resources.importer.api - This package contains the files from
 * Liferay's com.liferay.exportimport.resources.importer.internal.util package.  They are copied here
 * so they can be exposed externally as an API.
 *
 * So all of these classes are straight-up Liferay classes but with a few modifications.  I had to
 * update all of the package references, of course, but I also removed some static util class usage
 * and opened up more of the OSGi injection aspects for subclass support.
 *
 * Relatively minor changes to support custom utilization, but I think it is worth it.
 */
package com.liferay.exportimport.resources.importer.api;