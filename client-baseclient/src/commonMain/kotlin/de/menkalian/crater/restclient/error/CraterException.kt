package de.menkalian.crater.restclient.error

class CraterException(val error: CraterError): Exception("An error occured while accessing the crater server: $error")