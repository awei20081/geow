package org.geow.serializer

import org.geow.model._
import org.geow.model.geometry._

object OsmDenormalizedSerializer {

  import scala.pickling._
  import binary._

  def fromBinary(encoded: Array[Byte]): OsmDenormalizedObject = encoded.unpickle[OsmDenormalizedObject]

  def toBinary(decoded: OsmDenormalizedObject): Array[Byte] = decoded.pickle.value

  def fromGeoJson(geojson: String):List[OsmDenormalizedObject] =
    GeoJsonSerialiser
      .featureCollectionFromJSON(geojson)
      .map(OsmDenormalisedGeoJSONBijections.geoJsonToDenormalized)
      .getOrElse(Nil)
}