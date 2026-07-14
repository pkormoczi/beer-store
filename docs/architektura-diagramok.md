# Architektúra-diagramok

Ez a dokumentum a `beer-store-application` belső architektúráját vizualizálja Mermaid ábrákkal:
hogyan működik a Spring Modulith modulfelosztás, mire való az egyes package, és hogyan halad a
kommunikáció a portokon keresztül modulon belül és modulok között.

**Viszonya a generált dokumentációhoz:** a Spring Modulith `Documenter` API
(`architecture/ModularityTests.writesDocumentation()`) minden build futtatáskor legenerálja a
modul-térkép C4-PlantUML változatát a `beer-store-application/target/spring-modulith-docs/`
alá (`components.puml`, `module-*.puml`) — ez git-ignore-olt, gépi generált, és csak modul
szintű. Az itt található ábrák **nem váltják ki**, hanem kiegészítik azt: az 1. ábra a generált
modul-térkép GitHub-on olvasható, annotált párja, a 2–4. ábra pedig olyan mélységet mutat be
(package-szerepek, konkrét kérésfolyam, ACL-kommunikáció), amit a generált diagram nem tartalmaz.
Ezeket kézzel kell karban tartani — lásd a dokumentum végén a Karbantartás szakaszt.

A modulok és rétegek elnevezési konvencióját a gyökér `CLAUDE.md` "Spring Modulith module layout"
és "Naming & convention cheat-sheet" szakaszai írják le részletesen; ez a dokumentum azokra épül.

---

## 1. ábra — Modul-térkép (a modulith működés áttekintése)

A négy `@ApplicationModule` modul — `customer`, `product`, `order`, `shared` — és a `platform`
package, ami **szándékosan nem** Modulith-modul (`spring.modulith.detection-strategy=explicitly-annotated`,
lásd ADR-04). Az élek az egyes `package-info.java`-kban deklarált `allowedDependencies`
értékeket mutatják: `order` kizárólag a `customer::api` és `product::api` named interface-eken
át függ a másik két üzleti modultól (sosem a domainjükön), mindhárom üzleti modul függ a nyitott
(`OPEN`) `shared` modultól, a `platform` pedig egy keresztmetsző technikai package, amit a
bejövő adapterek használnak (biztonság, hibakezelés, megfigyelhetőség), de ami maga sosem
függ vissza egy üzleti modulra — ezt a határt nem a Modulith `verify()`, hanem külön ArchUnit
szabályok (`PlatformBoundaryTest`) őrzik.

```mermaid
flowchart TD
    subgraph app["beer-store-application · dev.ronin.demo.beerstore"]
        order["<b>order</b><br/>allowedDependencies:<br/>customer::api, product::api, shared"]
        customer["<b>customer</b><br/>allowedDependencies: shared"]
        product["<b>product</b><br/>allowedDependencies: shared"]
        shared["<b>shared</b> · @ApplicationModule(OPEN)<br/>kernel.Money · generált API · HomeController"]
        platform["<b>platform</b> · NEM Modulith-modul (plain package)<br/>security · rest · observability · openapi · ws<br/>(határok: PlatformBoundaryTest, ArchUnit)"]
    end

    order -->|uses| customer
    order -->|uses| product
    customer -->|depends on| shared
    product -->|depends on| shared
    order -->|depends on| shared
    order -.->|inbound adapterek használják| platform
    customer -.->|inbound adapterek használják| platform
    product -.->|inbound adapterek használják| platform
```

## 2. ábra — Hexagonális port-metszet + package-szerepek

Egy modul (példa: `order`) teljes ports-and-adapters gyűrűje, minden csomópont felcímkézve a
package-ével és a szerepével. Ez válaszolja meg egyszerre, hogy "melyik package mire való" és
"hogyan halad a kommunikáció a portokon": bejövő adapter → `api` (a modul egyetlen publikus
felülete) → mag (`application.service` + `domain`, amit a Modulith alapból elrejt) → kimenő
portok (`application.port.out`, interfészek) → kimenő adapterek. A `customer` és `product`
modul ugyanilyen alakú (a `customer` emellett egy `adapter.in.soap` bejövő adaptert is tartalmaz
a SOAP végponthoz).

```mermaid
flowchart LR
    http([HTTP / SOAP]):::ext

    subgraph inbound["adapter.in — bejövő adapterek"]
        ctrl["rest.OrderController<br/>@RestController"]
        radapter["rest.OrderRestAdapter @Service<br/>DTO ↔ command/view"]
        rmap["rest.OrderMapper<br/>rest.OrderRestExceptionHandler"]
    end

    subgraph api["api — a modul EGYETLEN publikus felülete · @NamedInterface"]
        mgmt["OrderManagement<br/>(port-in, interfész)"]
        dtos["command/ · query/ · view/ · type/ · exception/"]
    end

    subgraph coremod["application + domain — a modul magja (Modulith elrejti)"]
        svc["application.service.Orders<br/>@Service implements OrderManagement"]
        dom["domain.model.Order / OrderLine<br/>domain.event.OrderPlaced"]
    end

    subgraph portsout["application.port.out — kimenő portok (interfészek)"]
        repoPort["OrderRepository"]
        aclPort["CustomerLookup · BeerLookup · BeerSnapshot<br/>(ACL portok)"]
    end

    subgraph outbound["adapter.out — kimenő adapterek"]
        persist["persistence.jpa.OrderPersistenceAdapter<br/>+ OrderJpaRepository + entity.*JpaEntity"]
        acl["customer.CustomerLookupAdapter<br/>product.BeerLookupAdapter<br/>@Component, package-private"]
    end

    db[("Postgres")]:::ext
    foreign["customer::CustomerManagement<br/>product::BeerManagement"]:::ext

    http --> ctrl --> radapter --> mgmt
    mgmt -.->|implements| svc
    svc --> dom
    svc --> repoPort --> persist --> db
    svc --> aclPort --> acl --> foreign

    classDef ext fill:#eee,stroke:#999,color:#333;
```

## 3. ábra — Rendelés végigfutása a portokon (sequence)

A `placeOrder` konkrét, végponttól végpontig tartó folyamata — ez teszi kézzelfoghatóvá, hogy a
service (`Orders`) sosem hív közvetlenül idegen modult, hanem kizárólag a saját kimenő
portjain (`CustomerLookup`, `BeerLookup`) keresztül, és hogy az `OrderPlaced` esemény a
Spring Modulith JDBC event-publication registryn át, aszinkron, modulon belül fut le. A
lépéssorrend 1:1 megfelel az `order/application/service/Orders.java` `placeOrder` metódusának.

```mermaid
sequenceDiagram
    autonumber
    participant C as HTTP kliens
    participant Ctrl as OrderController
    participant RA as OrderRestAdapter
    participant Svc as Orders (OrderManagement)
    participant CL as CustomerLookup →<br/>CustomerLookupAdapter
    participant CM as customer::CustomerManagement
    participant BL as BeerLookup →<br/>BeerLookupAdapter
    participant BM as product::BeerManagement
    participant Repo as OrderRepository →<br/>OrderPersistenceAdapter
    participant DB as Postgres
    participant EV as OrderPlacedEventListener

    C->>Ctrl: POST /orders (OrderDto)
    Ctrl->>RA: addOrder(orderDto)
    RA->>Svc: placeOrder(PlaceOrder)
    Svc->>CL: assertCustomerExists(customerId)
    CL->>CM: getCustomer(GetCustomer)
    Note over CL,CM: CustomerNotFoundException, ha nincs ilyen vevő
    Svc->>BL: findExisting(beerIds)
    BL->>BM: findAllById(FindBeers)
    BM-->>BL: BeerView lista
    BL-->>Svc: BeerSnapshot lista (BeerView → saját DTO)
    Note over Svc: UnknownBeerException, ha hiányzik egy id
    Svc->>Svc: Order.place(customerId, lines) — domain aggregátum
    Svc->>Repo: save(order)
    Repo->>DB: INSERT orders / order_lines
    Svc-)EV: publishEvent(OrderPlaced) — aszinkron, JDBC event registry
    Svc-->>RA: orderId
    RA-->>Ctrl: OrderDto
    Ctrl-->>C: 201 Created
```

## 4. ábra — Modulközi ACL kommunikáció

Ráközelít arra, hogyan éri el az `order` modul a `customer`/`product` modulokat **kizárólag** a
saját kimenő ACL portjain (`CustomerLookup`, `BeerLookup`) és azok package-private adapterein
keresztül, sosem importálva a másik modul domain típusait. Az adapter fordítja le az idegen
`BeerView`-t az `order` saját `BeerSnapshot` DTO-jára, így egy `BeerView` mezőváltozás csak az
adapterig gyűrűzik be, a service-t és a domaint nem érinti (lásd ADR-01).

```mermaid
flowchart LR
    subgraph ordermod["order modul"]
        svc["application.service.Orders"]
        subgraph ports["application.port.out — ACL portok"]
            cl["CustomerLookup"]
            bl["BeerLookup"]
            bs["BeerSnapshot<br/>(order SAJÁT DTO-ja)"]
        end
        subgraph aclad["adapter.out.* — ACL adapterek (package-private @Component)"]
            cla["customer.CustomerLookupAdapter"]
            bla["product.BeerLookupAdapter"]
        end
    end

    subgraph customerapi["customer :: api"]
        cm["CustomerManagement"]
        cq["query.GetCustomer"]
    end
    subgraph productapi["product :: api"]
        bm["BeerManagement"]
        bq["query.FindBeers · view.BeerView"]
    end

    svc --> cl --> cla --> cm
    svc --> bl --> bla --> bm
    cla -.->|csak létezés-ellenőrzés| cq
    bla -->|BeerView → BeerSnapshot| bs
    bla -.->|olvassa| bq
```

---

## Karbantartás

Ezek az ábrák **kézzel karbantartottak**, nem generáltak — ha a modul-wiring változik, itt is
frissíteni kell őket. Forrás, amivel egyeztetni kell módosításkor:

- **1. ábra** a `customer`, `product`, `order`, `shared` modulok `package-info.java` fájljainak
  `allowedDependencies` értékét tükrözi, és a `ModularityTests.writesDocumentation()` által
  generált `target/spring-modulith-docs/components.puml` annotált párja.
- **2. és 4. ábra** az `order` modul teljes forrásfáját tükrözi: `order/api/OrderManagement.java`,
  `order/application/service/Orders.java`, `order/application/port/out/*.java`,
  `order/adapter/in/rest/*.java`, `order/adapter/out/**/*.java`. A `CLAUDE.md`
  "Module reference" táblázata (`customer` / `product` / `order` oszlopok) ugyanezt a
  réteg-per-réteg felsorolást adja meg minden modulra — ha a tábla változik, ez a két ábra is
  frissítendő.
- **3. ábra** kifejezetten az `Orders.placeOrder` metódus lépéssorrendjét követi
  (`order/application/service/Orders.java`) — metódus-átnevezés vagy lépéssorrend-változás esetén
  frissítendő.

Renderelés ellenőrzése: bármely Mermaid-kompatibilis megjelenítő (GitHub natívan rendereli a
`.md` fájlban a ```mermaid blokkokat; helyben VS Code Mermaid-preview vagy a mermaid.live
szerkesztő is használható).
