RxNav Android SDK
=================

This is an attempt to provide a decent and complete Android SDK for the
[RxNav APIs](http://rxnav.nlm.nih.gov/).

Each API is abstracted by a simple class and its operations by methods.

The SDK provide Java abstraction from JSON through
[gson](https://github.com/google-gson/google-gson), an awesome library that converts Java objects
from and to JavaScript Object Notation.

```java
RxNorm.Drugs drugs = RxNorm.newInstance().getDrugs("Aspirin");
```

Some endpoints are bound and binding the whole API is just a matter of time and testing.

Install
-------

dependencies {
    compile 'ca.umontreal.iro.rxnav:0.0.1'
}