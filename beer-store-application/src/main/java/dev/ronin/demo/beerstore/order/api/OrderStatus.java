package dev.ronin.demo.beerstore.order.api;

/**
 * The order lifecycle. Each constant knows which states it may legally move to next, so illegal
 * transitions (e.g. shipping a cancelled order) are a domain invariant rather than something
 * every caller has to remember to check.
 */
public enum OrderStatus {

    NEW {
        @Override
        public boolean canTransitionTo(OrderStatus target) {
            return target == PROCESSING || target == CANCELLED;
        }
    },
    PROCESSING {
        @Override
        public boolean canTransitionTo(OrderStatus target) {
            return target == SHIPPED || target == CANCELLED;
        }
    },
    SHIPPED {
        @Override
        public boolean canTransitionTo(OrderStatus target) {
            return target == DELIVERED;
        }
    },
    DELIVERED {
        @Override
        public boolean canTransitionTo(OrderStatus target) {
            return false;
        }
    },
    CANCELLED {
        @Override
        public boolean canTransitionTo(OrderStatus target) {
            return false;
        }
    };

    public abstract boolean canTransitionTo(OrderStatus target);
}
