package ca.umontreal.iro.rxnav;

import android.os.Parcel;
import android.os.Parcelable;

import com.squareup.okhttp.OkHttpClient;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * RxImageAccess Applications Programming Interface (API) is an online offering from the National
 * Library of Medicine (NLM) Lister Hill National Center for Biomedical Communications’ Office of
 * High Performance Computing and Communications (OHPCC) for searching for and retrieving digital
 * images and associated metadata for prescription oral solid dosage formulations (tablets, gel
 * caps and capsules, etc. – commonly known as pills) from a publicly available, increasingly
 * comprehensive, standardized, curated database. RxImageAccess has the following features:
 * <p/>
 * <ul>
 * <li>Curated image database of over 2,300 prescription products representing over 6,000 NDCs.</li>
 * <li>RxImageAccess is a RESTful (representational state transfer) Web API.</li>
 * <li>Results are returned in either JavaScript Object Notation (JSON) or Extensible Markup Language (XML) encoding.</li>
 * <li>Search parameters can include pill color, shape, imprint (text on a pill), and size, and NDC (National Drug Code), RxCUI (RxNorm Concept Unique Identifier), and Structured Product Label (SPL) identifier (SetID).</li>
 * <li>Images include watermarks to identify them as having originated with the NLM.</li>
 * <li>Images can include an information panel that provides the name, dosage, manufacturer, shape, size, color, and imprint.</li>
 * <li>Output control parameters include image resolution, number of results, number of images per page, page number, and whether returned metadata are to include active ingredients, inactive ingredients, trade names, and generic names.</li>
 * <li>RxImageAccess image files are standardized in two layout formats and available in multiple resolutions to support online, mobile, and desktop application development.</li>
 * </ul>
 * <p/>
 * http://rximage.nlm.nih.gov/docs/doku.php?id=start
 *
 * @author Guillaume Poirier-Morency
 */
public class RxImageAccess extends RxNav {

    public static RxImageAccess newInstance(OkHttpClient httpClient) {
        return new RxImageAccess(httpClient);
    }

    /**
     * Initialize RxNav to use the public API at http://rximage.nlm.nih.gov
     */
    public RxImageAccess(OkHttpClient httpClient) {
        super(httpClient, "http", "rximage.nlm.nih.gov", 80, "/api/", "");
    }

    /**
     * RxImageAccess API returns uniform JSON responses.
     * <p/>
     * The specification can be accessed here http://rximage.nlm.nih.gov/rxImageAccess.json
     * <p/>
     * An {@link Iterator} is implemented as results are paginated.
     */
    public class ImageAccess implements Iterator<ImageAccess>, Parcelable {

        public class ReplyStatus implements Parcelable {
            public String success;
            public int imageCount;
            public int totalImageCount;
            public String date;
            public Map<String, String> matchedTerms;

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(success);
                dest.writeInt(imageCount);
                dest.writeInt(totalImageCount);
                dest.writeString(date);
                // todo: write the matchedTerms Map
            }
        }

        public class RxImage implements Parcelable {

            public class Relabeler implements Parcelable {

                /**
                 * Named @sourceNdc9
                 */
                public String sourceNdc9;
                public String[] ndc9;

                @Override
                public int describeContents() {
                    return 0;
                }

                @Override
                public void writeToParcel(Parcel dest, int flags) {
                    dest.writeString(sourceNdc9);
                    dest.writeStringArray(ndc9);
                }
            }

            public int id;
            public String ndc11;
            public int part;
            public String matchNdc;
            public Relabeler[] relabelersNdc9;
            public String status;
            public int rxcui;
            public String splSetId;
            public String acqDate;
            public String name;
            public String labeler;
            public String imageUrl;
            public int imageSize;
            public String attribution;

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeInt(id);
                dest.writeString(ndc11);
                dest.writeInt(part);
                dest.writeString(matchNdc);
                dest.writeParcelableArray(relabelersNdc9, flags);
                dest.writeString(status);
                dest.writeInt(rxcui);
                dest.writeString(splSetId);
                dest.writeString(acqDate);
                dest.writeString(name);
                dest.writeString(labeler);
                dest.writeString(imageUrl);
                dest.writeInt(imageSize);
                dest.writeString(attribution);
            }
        }

        public ReplyStatus replyStatus;

        public RxImage[] nlmRxImages;

        @Override
        public boolean hasNext() {
            return replyStatus.imageCount < replyStatus.totalImageCount;
        }

        @Override
        public ImageAccess next() {
            List<BasicNameValuePair> matchedTerms = new ArrayList<>();

            for (Map.Entry<String, String> e : replyStatus.matchedTerms.entrySet()) {
                matchedTerms.add(new BasicNameValuePair(e.getKey(), e.getValue()));
            }

            try {
                // todo: reuse the same api method and RxImageAccess instance
                return RxImageAccess.newInstance(httpClient).rxnav(matchedTerms.toArray(new BasicNameValuePair[matchedTerms.size()]));
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("this instance is immutable");
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(replyStatus, flags);
            dest.writeParcelableArray(nlmRxImages, flags);
        }
    }

    /**
     * This is the recommended image collection due to the inclusion of an information panel.
     *
     * @param query contains pretty much what ends in the HTTP query
     * @return
     * @throws IOException
     */
    public ImageAccess rxnav(NameValuePair... query) throws IOException {
        return request(ImageAccess.class, "rximage/1/rxnav", query);
    }

    /**
     * This collection is identical to the rxnav collection without the panel.
     *
     * @param query contains pretty much what ends in the HTTP query
     * @return
     * @throws IOException
     */
    public ImageAccess rxbase(NameValuePair... query) throws IOException {
        return request(ImageAccess.class, "rximage/1/rxbase", query);
    }

}
