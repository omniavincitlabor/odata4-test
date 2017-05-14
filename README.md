OData Test
==============================
This is my testcase for SDL OData (v4) combined with CQEngine for dynamic Queries.

For this I took the original https://github.com/sdl/odata-example and added some classes and changed the Strategy for quering data. 

### My Experiments

According to http://www.odata.org/getting-started/basic-tutorial/

* request entity collection
* request entity by id
* do simple query ($filter) and simple joins
* use $expand option
* use $top (not tried skip)
* use $count as option and as operation
* function calls


#### Metadata

http://localhost:8080/example.svc/$metadata#

* **does not contain** Functions or Actions or FunctionImports or ActionImports


#### Entity Collection with Inheritance

http://localhost:8080/example.svc/Persons?$expand=department \
(TestClient: see "Basic Query with Expand")

* Navigation Properties are present in JSON-Result **but not** in ATOM-Result

#### Request by ID

http://localhost:8080/example.svc/Persons('MyHero') \
(TestClient: see "Single-Object-Query")
 
* Query via Browser is working in ATOM and JSON
* TestClient **does not get any result**
(it seems that only data from feeds are retrieved, but ATOM content is only single entry, which is ignored)
* For TestClient I needed my own ODataClientQuery-Implementation for the Id to be quoted

#### Simple Queries

**Simple Field**

http://localhost:8080/example.svc/Persons?$filter=age gt 20 \
(TestClient: see "Complex Query")

* CQEngine integration seems to work ;-)
* Needed own ODataClientQuery-Implementation to support other than eq-Query

**Enum Field**

http://localhost:8080/example.svc/Schools?$filter=type eq SDL.OData.Example.SchoolType'SECONDARY'

* **does not work**
Error-Message: "The URI contains an incorrectly specified $filter option"
* NullPointerException when field is null
(when retrieving JSON as well as ATOM)

**Calculation in Query**

http://localhost:8080/example.svc/Persons?$filter=age add foo gt 30

* CQEngine integration seems to work ;-)
* No ODataClientQuery-Implementation currently

**Field in Nested Object**

http://localhost:8080/example.svc/Persons?$filter=department/company/name eq 'foo'&$expand=department

* CQEngine integration seems to work ;-)

**Nested Filter in Expand**

http://localhost:8080/example.svc/Companies?$expand=departments($filter=startswith(name,'Fin'))

* **not working**
(Filtering is not applied)

**Nested Expand**

http://localhost:8080/example.svc/Companies?$expand=departments($expand=persons)&$format=json

* **not working**
(Nested objects are not displayed)


#### Joins

http://localhost:8080/example.svc/Companies(1)/departments?$filter=startswith(name,'Fin')&$format=json \
(TestClient: see "Join-Query")

http://localhost:8080/example.svc/Departments(1)/persons('MyHero')/school?$format=json \
(TestClient: see "Nested Join-Query")

* works
* Needed own ODataClientQuery-Implementation to support Join-Query


#### Count vs. Count

http://localhost:8080/example.svc/Persons?$count=true \
vs. \
http://localhost:8080/example.svc/Persons/$count

* works (after some change to original strategy)

#### Functions

For Bound Functions I miss the Type the Function is bound to.

**Bound (Collection)**

http://localhost:8080/example.svc/Persons/SDL.OData.Example.GetAverageAge()

http://localhost:8080/example.svc/Persons/SDL.OData.Example.GetAllAboveAge(age=20) \
(TestClient: see "Function Query with Parameter")

* works

**Bound (Single Object)**

http://localhost:8080/example.svc/Persons('MyHero')/SDL.OData.Example.GetBlubb()

* works

**Unbound**

http://localhost:8080/example.svc/SDL.OData.Example.GetAveragePersonAge()

* **does not work**
(Error Message: `?' expected but `S' found)
