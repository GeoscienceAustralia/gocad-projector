## Geoscience Australia ##

# GOCAD Projector #
This project contains a simple Java utility that can be used to reproject GOCAD objects between
different map projections. It uses the GDAL library for reprojection.

It has good support for Atomic, TSurf, PLine, TSolid, and Well objects (anything that uses VRTX
or PVRTX). It also has limited support for Voxet, GSurf, and SGrid objects, but only changes the
coordinate system and not individual points.

## Usage ##
There are two different executable classes in the utility; a GUI version and a Console version.

The GUI version contains a user interface which simplifies interaction with the utility. Simply
add the files you wish to reproject to the list, and click start. You can also reproject a
directory of GOCAD files.

The `Source SRS` and `Target SRS` parameters accept any string supported by GDAL's
`OGRSpatialReference.SetFromUserInput()` method, which includes EPSG PCS and GCSes (ie.
EPSG:4326), PROJ.4 declarations, or the name of a .prf file containing well known text (WKT).

The console version can be used to reproject GOCAD objects as a batch process. The command line
options are as follows:

    Usage: console [options]
      Options:
        -h, -help        Print these command line usage instructions
                         Default: false
      * -i, -input       The input GOCAD object to reproject.
      * -o, -output      The output repojected GOCAD object.
        -f, -overwrite   Force overwriting the output file if it already exists.
                         Default: false
      * -s, -s_srs       The source spatial reference set. The coordinate systems
                         that can be passed are anything supported by the
                         OGRSpatialReference.SetFromUserInput() call, which includes
                         EPSG PCS and GCSes (ie. EPSG:4326), PROJ.4 declarations (as
                         above), or the name of a .prf file containing well known
                         text.
      * -t, -t_srs       The target spatial reference set. The coordinate systems
                         that can be passed are anything supported by the
                         OGRSpatialReference.SetFromUserInput() call, which includes
                         EPSG PCS and GCSes (ie. EPSG:4326), PROJ.4 declarations (as
                         above), or the name of a .prf file containing well known
                         text.

## Supported platforms ##
Any architecture/operating system with compiled GDAL binaries and a JVM should be supported. However
this repository only contains the Windows (x86 and x64) GDAL binaries.

## License ##
This project is released under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0.html)
and is distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
CONDITIONS OF ANY KIND, either express or implied.

See the License for the specific language governing permissions and limitations under the License.

## Contact ##
For more information on this project, please email *m3dv:at:ga.gov.au*.