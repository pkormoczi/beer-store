/**
 * The customer aggregate module. Exposes its command/query/view DTOs (in the {@code api.command}/
 * {@code api.query}/{@code api.view} subpackages), the shared
 * {@link dev.ronin.demo.beerstore.customer.api.type.Address} value type and
 * {@link dev.ronin.demo.beerstore.customer.api.CustomerManagement} from the {@code api}
 * package (a Spring Modulith named interface, contributed to by every {@code api.*} subpackage);
 * every other subpackage ({@code domain}, {@code application}, {@code adapter}) is an
 * implementation detail hidden by Spring Modulith's default rule (module subpackages are
 * internal unless annotated {@code @NamedInterface}), including the {@code Customer} aggregate
 * itself. Its inbound REST adapter also uses the (Modulith-excluded) {@code platform}
 * package's {@code rest}/{@code security} types ({@code ErrorDetails}/{@code Authorized}) besides
 * the always-open {@code shared} module.
 */
@ApplicationModule(allowedDependencies = {"shared"})
package dev.ronin.demo.beerstore.customer;

import org.springframework.modulith.ApplicationModule;
