/**
 * Application-wide technical foundation: security, REST/error-handling and observability
 * concerns, plus the OpenAPI/SOAP wiring. Exposes three named interfaces - {@code security},
 * {@code rest} and {@code observability} - that business modules may depend on from their
 * inbound adapters; {@code openapi}/{@code ws} stay unexposed since nothing outside this module
 * needs them directly. Unlike the old open {@code infrastructure} module, {@code platform} is a
 * regular, closed module: it depends on nothing but {@code shared}, and - critically - never
 * depends back on a business module's domain exceptions or other API types (see {@code
 * platform.rest.CommonRestExceptionHandler} for why the per-module REST exception handlers live
 * in the business modules themselves rather than here).
 */
@ApplicationModule(allowedDependencies = {"shared"})
package dev.ronin.demo.beerstore.platform;

import org.springframework.modulith.ApplicationModule;
