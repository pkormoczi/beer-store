/**
 * Cross-cutting technical concerns: security (method-level authorization), logging and web
 * configuration. Open: any module may depend on it.
 */
@ApplicationModule(type = ApplicationModule.Type.OPEN)
package dev.ronin.demo.beerstore.infrastructure;

import org.springframework.modulith.ApplicationModule;
