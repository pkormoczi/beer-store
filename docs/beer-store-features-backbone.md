# beer-store — Feature Spec (Customer-side, Backbone)

> Spring Modulith + DDD + Clean Architecture showcase.
> Ez a dokumentum a **customer-oldali gerinc** (backbone) funkcióit specifikálja.
> Admin-oldal, tier-2 és nice-to-have feature-ök külön dokumentumba jönnek.

---

## 0. Draft-szintű döntések (overridable)

Ezek nem véglegesek, de a lenti spec ezekre épül. Ha bármelyiket megváltoztatod, jelöld, mert kihat az event-katalógusra.

| #  | Döntés | Alternatíva | Miért így |
|----|--------|-------------|-----------|
| D1 | **Payment = külön modul, async stub.** `PaymentConfirmed`/`PaymentFailed` eventet publikál vissza. | Szinkron `paid=true` flag az orderen. | Ez adja a sagát és a Modulith event-registry demót. Szinkron flag esetén nincs mit demonstrálni. |
| D2 | **Order tartja a state machine-t** (single source of truth a rendelés-életciklusra), a többi modul az Order eventjeire reagál és saját domain eventet küld vissza. | Tiszta orchestration (Order hívja a modulokat szinkron) vagy tiszta choreography (nincs központi állapot). | Realista hibrid: choreography a kommunikációban, de az állapot egy helyen van → debuggolható marad. |
| D3 | **Cart külön modul, de vékony.** Checkoutkor a tartalom + ár **snapshot**-olódik az Orderbe. | Cart az Ordering része / csak session state. | A snapshot-határ jó DDD tanpélda (a rendelés nem függ a cart későbbi mutációitól, és az ár a rendelés pillanatában rögzül). |
| D4 | **Choreographed saga event-ekkel**, Modulith event publication registry-vel (tranzakciós, at-least-once). | Külső message broker (Kafka). | A registry pont a Modulith killer feature-e; broker nélkül is demonstrálható az outbox-szerű megbízhatóság. |

---

## 1. Modul-térkép (context map)

| Modul | Felelősség | Függ (compile-time) | Reagál (event-time) |
|-------|-----------|---------------------|---------------------|
| `catalog` | Sör-termékek, stílusok, browse/search read-model | — | `inventory` eventjeire (availability read-model) |
| `cart` | Kosár életciklus, line item invariánsok | `catalog` (product ref/validáció) | — |
| `order` | Order aggregate, checkout, **state machine**, saga koordináció | `cart` (snapshot olvasás), `customer` | `inventory`, `payment` eventjeire |
| `inventory` | Készlet, reservation, low/out-of-stock jelzés | — | `order` eventjeire |
| `payment` | Fizetés stub (async confirm) | — | `order` eventjeire |
| `notification` | Async értesítés (stub: log/DB) | — | széles körben (`order`, `inventory`...) |
| `customer` | Account, auth, cím, rendeléstörténet | — | — |

**Alapelv:** modulok közti kommunikáció **kizárólag** publikus API-n (interface + DTO) vagy **domain eventen** keresztül. Belső package soha nem szivárog ki. `ApplicationModules.verify()` a build része.

---

## 2. Event-katalógus (a saga gerince)

Egy helyen minden domain event, hogy a modul-gráf átlátható legyen. Múlt idő = megtörtént tény.

| Event | Publisher | Consumer(ek) | Payload (lényeg) | Jelleg |
|-------|-----------|--------------|------------------|--------|
| `OrderPlaced` | `order` | `inventory` | orderId, lineItems (productId, qty), customerId | tény |
| `StockReserved` | `inventory` | `order` | orderId, reservationId | tény |
| `StockReservationFailed` | `inventory` | `order` | orderId, failedItems | tény |
| `PaymentRequested` | `order` | `payment` | orderId, amount, currency | tény (kérés megtörtént) |
| `PaymentConfirmed` | `payment` | `order` | orderId, paymentId | tény |
| `PaymentFailed` | `payment` | `order` | orderId, reason | tény |
| `OrderConfirmed` | `order` | `notification`, `inventory` | orderId, customerId | tény |
| `OrderCancelled` | `order` | `inventory`, `notification` | orderId, reason | tény (compensation trigger) |
| `StockReleased` | `inventory` | — | reservationId | tény (compensation eredmény) |
| `StockLevelChanged` | `inventory` | `catalog` | productId, availableQty | tény (read-model frissítés) |

**Compensation útvonal:** `PaymentFailed` → Order `CANCELLED` → `OrderCancelled` → Inventory `StockReleased`. Ez a saga rollback-ága, ezt is teszteld.

---

## 3. Feature-spec template

Ezt másold új feature-höz. Ami nem releváns (pl. read-only feature-nél event), azt hagyd ki, ne írj bele „N/A" sort.

```
### [MODUL] Feature neve — FEAT-XXX-N

**Modul:** melyik bounded context birtokolja
**Prioritás:** backbone / tier-2 / nice-to-have
**Típus:** command-flow / read-model / cross-module reaction

**Intent:** egy mondat, miért létezik.

**Aktorok:** customer / system

**Előfeltételek:**
- ...

**Fő flow (happy path):**
1. ...

**Domain szabályok / invariánsok:**
- (kikényszerített üzleti szabály, nem UI-validáció)

**Events published:**
**Events consumed:**

**API felület (customer-facing):**
- METHOD /path — mit csinál

**Read-model / query:**
- ...

**Acceptance criteria (testelhető):**
- Given ... When ... Then ...

**Nyitott döntés:**
- ...

**Out of scope (most):**
- ...
```

---

## 4. Backbone feature-ök

Ajánlott build-sorrend = ahogy itt következnek. **Vertikálisan** építs (egy szeletet végig eventekkel), ne szélességben.

---

### [catalog] Termék-böngészés, keresés, szűrés — FEAT-CAT-1

**Modul:** `catalog`
**Prioritás:** backbone
**Típus:** read-model

**Intent:** A customer megtalálja a megvásárolható söröket, szűrve stílus/ABV/ár szerint.

**Aktorok:** customer

**Előfeltételek:**
- Van legalább 1 aktív termék a katalógusban.

**Fő flow (happy path):**
1. Customer listázza a terméket (paginált).
2. Szűr (style, ABV-tartomány, ár-tartomány) és/vagy keres (név-szöveg).
3. Rendez (ár, név, újdonság).

**Domain szabályok / invariánsok:**
- Csak `ACTIVE` státuszú termék jelenik meg a customernek.
- Ár mindig pozitív, currency kötött (single-currency a demóban).

**Events consumed:**
- `StockLevelChanged` → availability read-model frissítése (in-stock / low / out-of-stock badge).

**API felület:**
- `GET /api/products?style=&minAbv=&maxAbv=&minPrice=&maxPrice=&q=&sort=&page=` — paginált lista
- `GET /api/products/{id}` — termék részletei + aktuális availability

**Read-model / query:**
- `ProductSummary` (lista): id, name, style, abv, price, availabilityStatus, thumbnail.
- `ProductDetail`: + leírás, batch/lejárat infó (ha van), részletes availability.

**Acceptance criteria:**
- Given 3 ACTIVE és 1 INACTIVE termék, When GET /api/products, Then csak a 3 ACTIVE jön vissza.
- Given style=IPA szűrő, When lekérés, Then csak IPA-k jönnek vissza.
- Given egy termék elfogy (`StockLevelChanged` availableQty=0), When részletlekérés, Then availabilityStatus = OUT_OF_STOCK.

**Nyitott döntés:**
- A search full-text (később) vagy sima `LIKE`/spec (most)? Javaslat: most `Specification`, full-text nice-to-have.

**Out of scope (most):**
- Ajánlások, pairing, review-alapú rendezés.

---

### [cart] Kosár-kezelés — FEAT-CART-1

**Modul:** `cart`
**Prioritás:** backbone
**Típus:** command-flow

**Intent:** A customer összeállít egy kosarat, amit majd checkoutol.

**Aktorok:** customer

**Előfeltételek:**
- Létező, `ACTIVE` termék az itemhez.

**Fő flow (happy path):**
1. Customer terméket ad a kosárhoz (productId, qty).
2. Módosít mennyiséget / töröl itemet.
3. Megnézi a kosarat (kalkulált subtotal).

**Domain szabályok / invariánsok:**
- Line item qty > 0 (0 = törlés).
- Item csak létező, `ACTIVE` termékre hivatkozhat (validáció `catalog` API-n).
- Cart **nem tárol árat élőben** — az árat megjelenítéskor a `catalog`-ból olvassa. Ár-rögzítés (snapshot) csak checkoutkor történik (lásd FEAT-ORD-1).
- Opcionális invariáns a demóhoz: per-item max qty (pl. limitált sörnél).

**Events published:** — (nincs; a cart mutáció nem publikus domain-esemény ebben a demóban)

**API felület:**
- `POST /api/cart/items` — item hozzáadás { productId, qty }
- `PATCH /api/cart/items/{productId}` — qty módosítás
- `DELETE /api/cart/items/{productId}` — item törlés
- `GET /api/cart` — aktuális kosár + kalkulált subtotal

**Read-model / query:**
- `CartView`: itemek (productId, name, unitPrice [élő catalog-ból], qty, lineTotal), subtotal.

**Acceptance criteria:**
- Given üres kosár, When POST item qty=2, Then kosárban 1 line, qty=2.
- Given kosárban egy item qty=2, When PATCH qty=5, Then qty=5, subtotal újraszámolva.
- Given nem létező productId, When POST item, Then 4xx, kosár változatlan.

**Nyitott döntés:**
- Cart-identitás: authenticated customerhez kötött vs. guest cart (cookie/token). Javaslat: most **authenticated only**, guest checkout tier-2 (állapotgép-különbség, nem külön feature).
- Reserved-e a készlet kosárba tevéskor? **Nem** — foglalás csak checkoutkor (FEAT-INV-1). A „kosárba tett = lefoglalt" invariáns bonyolít, tedd nice-to-have-be.

**Out of scope (most):**
- Guest cart, mixed-case builder, mentett kosár.

---

### [order] Checkout + rendelés-leadás (saga) — FEAT-ORD-1

**Modul:** `order` (state machine owner + saga koordinátor)
**Prioritás:** backbone — **ez a központi darab**
**Típus:** command-flow + cross-module orchestration

**Intent:** A customer kosarából rendelés lesz, ami készletfoglaláson és fizetésen át megerősítésig jut, hibánál rollbackel.

**Aktorok:** customer, system (async modulok)

**Előfeltételek:**
- Authenticated customer.
- Nem üres kosár.
- Megadott szállítási cím.

**Fő flow (happy path):**
1. Customer checkoutol: kosár + szállítási cím confirm.
2. Order modul **snapshotolja** a kosár tartalmát és a **catalog-árakat** → `Order` létrejön `PENDING` státuszban.
3. Order publikál `OrderPlaced`.
4. `inventory` reagál → foglal → `StockReserved`.
5. Order reagál → `AWAITING_PAYMENT` → publikál `PaymentRequested`.
6. `payment` reagál → (async stub) → `PaymentConfirmed`.
7. Order reagál → `CONFIRMED` → publikál `OrderConfirmed`.
8. `notification` reagál → megerősítő értesítés.

**Order state machine:**
```
PENDING ──OrderPlaced──▶ (inventory)
   │
   ├─ StockReserved ──▶ AWAITING_PAYMENT ──PaymentRequested──▶ (payment)
   │                          │
   │                          ├─ PaymentConfirmed ──▶ CONFIRMED ──▶ OrderConfirmed
   │                          └─ PaymentFailed ─────▶ CANCELLED  ──▶ OrderCancelled
   │
   └─ StockReservationFailed ─▶ CANCELLED ──▶ OrderCancelled
```

**Domain szabályok / invariánsok:**
- Rendelés **immutábilis snapshotot** tárol: line itemek + ár a rendelés pillanatában. Későbbi catalog ár- vagy készletváltozás a leadott rendelést nem érinti.
- Order total = Σ lineTotal (+ szállítás, ha FEAT-ORD-2 megvan; most csak subtotal).
- Állapotátmenetek **csak** a state machine szerint (érvénytelen átmenet → hiba).
- Idempotencia: ugyanaz az event kétszer ne léptesse tovább kétszer az állapotot (Modulith registry újraküldhet — at-least-once).

**Events published:** `OrderPlaced`, `PaymentRequested`, `OrderConfirmed`, `OrderCancelled`
**Events consumed:** `StockReserved`, `StockReservationFailed`, `PaymentConfirmed`, `PaymentFailed`

**API felület:**
- `POST /api/checkout` — { shippingAddressId } → létrehoz rendelést, visszaad orderId + kezdeti státusz
- `GET /api/orders/{id}` — rendelés + aktuális státusz (a customer így pollolja/nézi a saga eredményét)

**Read-model / query:**
- `OrderView`: id, status, snapshotolt itemek, total, createdAt, timeline (státuszváltások).

**Acceptance criteria:**
- Given nem üres, in-stock kosár When POST /api/checkout, Then Order `PENDING`, és `OrderPlaced` bekerül a publication registry-be.
- Given `StockReserved` beérkezik, When Order feldolgozza, Then státusz `AWAITING_PAYMENT`, `PaymentRequested` publikálva.
- Given `PaymentConfirmed`, When feldolgozás, Then `CONFIRMED`, `OrderConfirmed` publikálva.
- Given `PaymentFailed`, When feldolgozás, Then `CANCELLED`, `OrderCancelled` publikálva, és a foglalás felszabadul (`StockReleased`).
- Given `StockReservationFailed`, When feldolgozás, Then `CANCELLED`, payment sosem hívódik.
- Given ugyanaz a `PaymentConfirmed` kétszer, When feldolgozás, Then állapot csak egyszer változik (idempotens).

**Nyitott döntés:**
- Choreography vs orchestration finomhangolás (D2). Ahogy leírtam: állapot Orderben, kommunikáció eventtel.
- Customer hogy értesül a saga végéről? Polling (`GET /orders/{id}`) most, SSE/websocket nice-to-have.

**Out of scope (most):**
- Szállítási díj, adó, promó (FEAT-ORD-2 / Pricing tier-2).
- Rész-teljesítés (partial fulfillment).

---

### [inventory] Készletfoglalás és availability — FEAT-INV-1

**Modul:** `inventory`
**Prioritás:** backbone
**Típus:** cross-module reaction

**Intent:** Rendeléskor lefoglalja a készletet, hibánál elutasít, megerősítésnél véglegesít, rollbacknél felszabadít; availability-t jelez a catalognak.

**Aktorok:** system

**Fő flow (happy path):**
1. `OrderPlaced` beérkezik.
2. Ellenőrzi minden itemre az elérhető készletet.
3. Elég → foglal (reservation rekord) → `StockReserved` + `StockLevelChanged`.
4. `OrderConfirmed`-nál a foglalás véglegesítése (available már csökkent, most a foglalás „elhasználódik").

**Compensation flow:**
1. `OrderCancelled` beérkezik → foglalás felszabadítása → `StockReleased` + `StockLevelChanged`.

**Domain szabályok / invariánsok:**
- `availableQty = onHand - reserved`. Foglalás soha nem viheti negatívba az available-t.
- Foglalás orderId-hez kötött, idempotens (ugyanarra az orderId-re egyszer foglal).
- `StockLevelChanged` küszöbökkel: OUT_OF_STOCK (0), LOW (< küszöb), IN_STOCK.

**Events published:** `StockReserved`, `StockReservationFailed`, `StockReleased`, `StockLevelChanged`
**Events consumed:** `OrderPlaced`, `OrderConfirmed`, `OrderCancelled`

**API felület:**
- (customer-facing közvetlen nincs; az availability a `catalog` read-modelen át látszik)

**Acceptance criteria:**
- Given onHand=10 reserved=0, When `OrderPlaced` qty=3, Then reserved=3, `StockReserved` publikálva.
- Given onHand=2, When `OrderPlaced` qty=3, Then `StockReservationFailed`, reserved változatlan.
- Given aktív foglalás, When `OrderCancelled`, Then reserved csökken, `StockReleased` + `StockLevelChanged`.
- Given onHand=reserved (available=0), When `StockLevelChanged`, Then status OUT_OF_STOCK a catalogban.

**Nyitott döntés:**
- Foglalásnak van-e TTL-je (lejár, ha a fizetés soká tart)? Javaslat: most nincs; TTL/expiry nice-to-have (scheduler + event, jó Modulith demo későbbre).

**Out of scope (most):**
- Batch/lejárat szerinti FEFO-kiadás, több raktár.

---

### [payment] Fizetés stub — FEAT-PAY-1

**Modul:** `payment`
**Prioritás:** backbone
**Típus:** cross-module reaction

**Intent:** Az async fizetés-visszaigazolást szimulálja, hogy a saga teljes legyen — külső integráció nélkül.

**Aktorok:** system

**Fő flow (happy path):**
1. `PaymentRequested` beérkezik.
2. Stub „feldolgoz" (konfigurálható: azonnali / késleltetett / determinisztikus siker/hiba pl. összeg alapján a teszteléshez).
3. `PaymentConfirmed` (vagy hiba-ágon `PaymentFailed`).

**Domain szabályok / invariánsok:**
- Egy orderId-re egy fizetés (idempotens; duplikált `PaymentRequested` nem indít újat).
- Payment rekord audit-célra megmarad (paymentId, orderId, status, amount).

**Events published:** `PaymentConfirmed`, `PaymentFailed`
**Events consumed:** `PaymentRequested`

**Acceptance criteria:**
- Given `PaymentRequested` amount>0, When feldolgozás, Then `PaymentConfirmed` payment rekorddal.
- Given teszt-trigger a hibához (pl. speciális amount), When feldolgozás, Then `PaymentFailed` reason-nel.
- Given duplikált `PaymentRequested` ugyanarra az orderId-re, Then csak egy payment rekord.

**Nyitott döntés:**
- Hiba-injektálás módja: config flag vs. „magic" amount vs. dedikált teszt-endpoint. Javaslat: config-vezérelt strategy, hogy integ-tesztben determinisztikus legyen.

**Out of scope (most):**
- Valódi PSP, 3DS, refund, részfizetés.

---

### [notification] Rendelés-megerősítés értesítés — FEAT-NOTIF-1

**Modul:** `notification`
**Prioritás:** backbone
**Típus:** cross-module reaction

**Intent:** Eventekre reagálva értesíti a customert (stub: DB-be ír / logol, nem küld valódi emailt).

**Aktorok:** system

**Fő flow (happy path):**
1. `OrderConfirmed` beérkezik.
2. Notification rekord létrehozása (customerId, típus, tartalom).

**Domain szabályok / invariánsok:**
- Idempotens: egy `OrderConfirmed` = egy notification.

**Events consumed:** `OrderConfirmed` (később: `OrderCancelled`, `StockReleased` stb.)

**API felület:**
- `GET /api/notifications` — a bejelentkezett customer értesítései (in-app inbox a frontendnek)

**Acceptance criteria:**
- Given `OrderConfirmed`, When feldolgozás, Then 1 notification rekord a customerhez.
- Given ugyanaz az event kétszer, Then csak 1 notification.

**Nyitott döntés:**
- Csatorna-absztrakció (in-app / email / SMS) most kell-e? Javaslat: interface + egy in-app impl; a többi csatorna nice-to-have.

**Out of scope (most):**
- Valódi email küldés, template-motor, preferenciák.

---

### [customer] Account, auth, rendeléstörténet — FEAT-CUST-1

**Modul:** `customer`
**Prioritás:** backbone (de alacsony showcase-hozam — ne pazarolj rá időt)
**Típus:** command-flow + read-model

**Intent:** Regisztráció/bejelentkezés, cím-kezelés, korábbi rendelések megtekintése.

**Aktorok:** customer

**Fő flow (happy path):**
1. Regisztráció / login (Spring Security default).
2. Szállítási cím(ek) kezelése.
3. Rendeléstörténet megtekintése.

**Domain szabályok / invariánsok:**
- Rendeléstörténet a bejelentkezett customer saját rendeléseire szűrt (nincs cross-customer olvasás).

**API felület:**
- `POST /api/auth/register`, `POST /api/auth/login`
- `GET/POST/PUT/DELETE /api/customer/addresses`
- `GET /api/customer/orders` — saját rendelések (az `order` read-modelt olvassa a publikus API-n át)

**Acceptance criteria:**
- Given bejelentkezett customer 2 rendeléssel, When GET /api/customer/orders, Then csak az ő 2 rendelése.
- Given nem bejelentkezett, When védett endpoint, Then 401.

**Nyitott döntés:**
- Az `order`-history a `customer` modulban aggregálódik, vagy a frontend hívja külön az `order` API-t? Javaslat: frontend külön hívja az `order` read-modelt — kevesebb cross-module csatolás. Emiatt lehet, hogy a `GET /api/customer/orders` inkább `order` modulban él (`GET /api/orders?mine=true`). **Döntsd el, mert modul-ownership kérdés.**

**Out of scope (most):**
- Social login, jelszó-reset flow, profil-kép, GDPR export.

---

## 5. Cross-cutting a gerinchez (ne feledd)

- **`ApplicationModuleTest`** modulonként — a modul izoláltan tesztelhető, függőségek stubbolva.
- **`ApplicationModules.verify()`** a buildben — architektúra-szabály CI-gate.
- **Event publication registry** bekapcsolva → incomplete publikációk láthatók, restartnál újraküldhetők (ez a megbízhatóság-demó).
- **Idempotencia** minden event-consumernél (at-least-once miatt) — ez visszatérő acceptance criteria fent.
- **Modulith dokumentáció-generálás** (`Documenter`) — a modul-térkép és a C4-szerű ábra generálva, nem kézzel karbantartva.

## 6. Következő lépések (nem ebben a doksiban)

- Tier-2: szállítási cím + díj, régió-alapú alkohol-tiltás, DRS/betétdíj a pricingben, guest checkout.
- Nice-to-have: back-in-stock waitlist (tiszta event-driven demo), beer club subscription, mixed-case builder, reviews/wishlist.
