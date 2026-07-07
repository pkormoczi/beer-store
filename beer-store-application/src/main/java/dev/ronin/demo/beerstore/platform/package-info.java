/**
 * Application-wide technical foundation: security, REST/error-handling and observability
 * concerns, plus the OpenAPI/SOAP wiring. Deliberately a plain package, not a Spring Modulith
 * module - {@code ModularityTests} excludes {@code platform} from the {@code ApplicationModules}
 * scan entirely, so Modulith validates only the actual business domains (customer, product,
 * order). The two invariants Modulith would otherwise have enforced here are instead ArchUnit
 * rules in {@code PlatformBoundaryTest}: {@code platform} never depends back on a business
 * module's domain exceptions or other API types (see {@code platform.rest.CommonRestExceptionHandler}
 * for why the per-module REST exception handlers live in the business modules themselves rather
 * than here), and the {@code openapi}/{@code ws} subpackages are never accessed from outside
 * {@code platform}. Naming conventions (e.g. {@code *Config}) are still checked by
 * {@code PlatformArchitectureTest}.
 */
package dev.ronin.demo.beerstore.platform;
