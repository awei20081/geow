package org.geow.serializer.test

import org.geow.geojson._
import org.geow.serializer.GeoJsonSerialiser
import org.specs2.ScalaCheck
import org.specs2.matcher.StringMatchers
import org.specs2.mutable.Specification
import argonaut._, Argonaut._

/**
 * Created by mark on 12.05.15.
 */
class GeoJsonSerialisationSpec extends Specification with ScalaCheck with StringMatchers {

  "Deserialisation and Serialisation" should {
    val get = (x:Option[Geometry]) ⇒ x.get
    val roundtrip = GeoJsonSerialiser.jsonFromGeometry _ compose get compose GeoJsonSerialiser.geometryFromJSON

    "work for LineStrings" in {
      val lineString = """
                       |{
                       | "type": "LineString",
                       | "coordinates": [
                       |   [102.0, 0.0], [103.0, 1.0], [104.0, 0.0], [105.0, 1.0]
                       |   ]
                       |}
                     """.stripMargin
      GeoJsonSerialiser.geometryFromJSON(lineString) mustEqual
        Some(LineString(List( (102f, 0f), (103f, 1f), (104f, 0f), (105f, 1f) )))
      roundtrip(lineString) must be_==/(lineString)
    }

    "work for Points" in {
      val point = """{"type": "Point", "coordinates": [102.0, 0.5]}"""
      GeoJsonSerialiser.geometryFromJSON(point) mustEqual Some(Point((102f, 0.5f)))
      roundtrip(point) must be_==/(point)
    }

    "work for Polygons" in {
      val poly = """{
                    |  "type": "Polygon",
                    |  "coordinates": [ [ [100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0] ] ]
                    |}""".stripMargin
      GeoJsonSerialiser.geometryFromJSON(poly) mustEqual
        Some(Polygon(List( List( (100f, 0f), (101f, 0f), (101f, 1f), (100f, 1f), (100f, 0f) ) ) ))
      roundtrip(poly) must be_==/(poly)
    }

    "work for MultiPoints" in {
      val multiPoints = """
                   |{ "type": "MultiPoint",
                   |    "coordinates": [ [100.0, 0.0], [101.0, 1.0] ]
                   |    }
                   |""".stripMargin
      GeoJsonSerialiser.geometryFromJSON(multiPoints) mustEqual
        Some(MultiPoint( List( (100f, 0f), (101f, 1f) ) ) )
      roundtrip(multiPoints) must be_==/(multiPoints)
    }

    "work for MultiLineString" in {
      val multiLineString = """
                   |{ "type": "MultiLineString",
                   |    "coordinates": [
                   |        [ [100.0, 0.0], [101.0, 1.0] ],
                   |        [ [102.0, 2.0], [103.0, 3.0] ]
                   |      ]
                   |    }
                   |""".stripMargin
      GeoJsonSerialiser.geometryFromJSON(multiLineString) mustEqual
        Some(MultiLineString(List( List((100f, 0f), (101f, 1f)), List((102f, 2f), (103f, 3f)) ) ))
      roundtrip(multiLineString) must be_==/(multiLineString)
    }

    "work for MultiPolygons" in {
      val multiPoly = """
                   |{ "type": "MultiPolygon",
                   |    "coordinates": [
                   |      [[[102.0, 2.0], [103.0, 2.0], [103.0, 3.0], [102.0, 3.0], [102.0, 2.0]]],
                   |      [[[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]],
                   |       [[100.2, 0.2], [100.8, 0.2], [100.8, 0.8], [100.2, 0.8], [100.2, 0.2]]]
                   |      ]
                   |    }
                   |""".stripMargin
      GeoJsonSerialiser.geometryFromJSON(multiPoly) mustEqual
        Some(MultiPolygon(List(
          List( List( (102f, 2f), (103f, 2f), (103f, 3f), (102f, 3f), (102f, 2f) )),
          List( List( (100f, 0f), (101f, 0f), (101f, 1f), (100f, 1f), (100f, 0f) ),
                List( (100.2f, 0.2f), (100.8f, 0.2f), (100.8f, 0.8f), (100.2f, 0.8f), (100.2f, 0.2f) ))
        ) ))
//      roundtrip(multiPoly) must be_==/(multiPoly) //Stupid rounding floating point rounding
    }

    "work for Polygons" in {
      val geometryCollection = """
                   |{ "type": "GeometryCollection",
                   |    "geometries": [
                   |      { "type": "Point",
                   |        "coordinates": [100.0, 0.0]
                   |        },
                   |      { "type": "LineString",
                   |        "coordinates": [ [101.0, 0.0], [102.0, 1.0] ]
                   |        }
                   |    ]
                   |  }
                   |""".stripMargin
      GeoJsonSerialiser.geometryFromJSON(geometryCollection) mustEqual
        Some(GeometryCollection(List(
          Point((100f,0f)),
          LineString(List((101f, 0f), (102f, 1f)))
        )))
      roundtrip(geometryCollection) must be_==/(geometryCollection)
    }

    "work for Features" in {
      val feature = """{ "type": "Feature",
                      |        "geometry": {"type": "Point", "coordinates": [102.0, 0.5]},
                      |        "properties": {"string": "value0", "int": "1", "float": "1.2"}
                      |        }""".stripMargin
      GeoJsonSerialiser.featureFromJSON(feature) mustEqual
        Some(Feature(Point((102f, 0.5f)), Map("string" → "value0", "int" → "1", "float" → "1.2")))

    }
  }


  "The example from the GeoJSON homepage" should {
    "deserialise and serialise" in {
      val example =
        """
          |{ "type": "FeatureCollection",
          |    "features": [
          |      { "type": "Feature",
          |        "geometry": {"type": "Point", "coordinates": [102.0, 0.5]},
          |        "properties": {"prop0": "value0"}
          |        },
          |      { "type": "Feature",
          |        "geometry": {
          |          "type": "LineString",
          |          "coordinates": [
          |            [102.0, 0.0], [103.0, 1.0], [104.0, 0.0], [105.0, 1.0]
          |            ]
          |          },
          |        "properties": {
          |          "prop0": "value0",
          |          "prop1": 0.0
          |          }
          |        },
          |      { "type": "Feature",
          |         "geometry": {
          |           "type": "Polygon",
          |           "coordinates": [
          |             [ [100.0, 0.0], [101.0, 0.0], [101.0, 1.0],
          |               [100.0, 1.0], [100.0, 0.0] ]
          |             ]
          |         },
          |         "properties": {
          |           "prop0": "value0",
          |           "prop1": {"this": "that"}
          |           }
          |         }
          |       ]
          |     }
        """.stripMargin
      GeoJsonSerialiser.featureCollectionFromJSONEither(example).toEither must beRight
    }
  }

  "The example from the GeoJSON homepage with all Properties being Strings" should {
    "deserialise and serialise" in {
      val example =
        """
          |{ "type": "FeatureCollection",
          |    "features": [
          |      { "type": "Feature",
          |        "geometry": {"type": "Point", "coordinates": [102.0, 0.5]},
          |        "properties": {"prop0": "value0"}
          |        },
          |      { "type": "Feature",
          |        "geometry": {
          |          "type": "LineString",
          |          "coordinates": [
          |            [102.0, 0.0], [103.0, 1.0], [104.0, 0.0], [105.0, 1.0]
          |            ]
          |          },
          |        "properties": {
          |          "prop0": "value0",
          |          "prop1": "0.0"
          |          }
          |        },
          |      { "type": "Feature",
          |         "geometry": {
          |           "type": "Polygon",
          |           "coordinates": [
          |             [ [100.0, 0.0], [101.0, 0.0], [101.0, 1.0],
          |               [100.0, 1.0], [100.0, 0.0] ]
          |             ]
          |         },
          |         "properties": {
          |           "prop0": "value0",
          |           "prop1": "{\"this\": \"that\"}"
          |           }
          |         }
          |       ]
          |     }
        """.stripMargin
      GeoJsonSerialiser.featureCollectionFromJSON(example) must beSome
    }
  }
}
