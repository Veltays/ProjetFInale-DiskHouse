package DiskHouse.model.Utils;

import DiskHouse.model.entity.Playlist;
import DiskHouse.model.entity.Musique;
import DiskHouse.model.entity.Album;
import DiskHouse.model.entity.Artiste;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ImportXml implements IImport {
    @Override
    public List<Playlist> importData(String filePath) throws Exception {
        List<Playlist> playlists = new ArrayList<>();
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(new File(filePath));
        doc.getDocumentElement().normalize();
        NodeList playlistNodes = doc.getElementsByTagName("playlist");
        for (int i = 0; i < playlistNodes.getLength(); i++) {
            Node pNode = playlistNodes.item(i);
            if (pNode.getNodeType() == Node.ELEMENT_NODE) {
                Element pElem = (Element) pNode;
                String nomPlaylist = getTagValue(pElem, "nomPlaylist");
                int nombreMusique = parseIntSafe(getTagValue(pElem, "nombreMusique"));
                String coverImageURL = getTagValue(pElem, "coverImageURL");
                // Musiques
                List<Musique> musiques = new ArrayList<>();
                NodeList musiquesNodes = pElem.getElementsByTagName("musique");
                for (int j = 0; j < musiquesNodes.getLength(); j++) {
                    Node mNode = musiquesNodes.item(j);
                    if (mNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element mElem = (Element) mNode;
                        String titre = getTagValue(mElem, "titre");
                        float duree = parseFloatSafe(getTagValue(mElem, "duree"));
                        String albumTitre = getTagValue(mElem, "album");
                        Album album = albumTitre.isEmpty() ? null : new Album(albumTitre, null, null);
                        String coverMusique = getTagValue(mElem, "coverImageURL");
                        LocalDate dateAjout = parseDateSafe(getTagValue(mElem, "dateAjout"));
                        // Artistes
                        List<Artiste> artistes = new ArrayList<>();
                        NodeList artistesNodes = mElem.getElementsByTagName("artiste");
                        for (int k = 0; k < artistesNodes.getLength(); k++) {
                            Node aNode = artistesNodes.item(k);
                            if (aNode.getNodeType() == Node.ELEMENT_NODE) {
                                String pseudo = aNode.getTextContent();
                                artistes.add(new Artiste(pseudo, null));
                            }
                        }
                        Musique musique = new Musique(titre, duree, album, artistes, coverMusique);
                        if (dateAjout != null) {
                            // setter si possible, sinon ignorer (constructeur met la date du jour)
                        }
                        musiques.add(musique);
                    }
                }
                Playlist playlist = new Playlist(nomPlaylist, musiques);
                playlist.setCoverImageURL(coverImageURL);
                playlists.add(playlist);
            }
        }
        return playlists;
    }

    private String getTagValue(Element elem, String tag) {
        NodeList nl = elem.getElementsByTagName(tag);
        if (nl.getLength() > 0 && nl.item(0).getFirstChild() != null) {
            return nl.item(0).getFirstChild().getNodeValue();
        }
        return "";
    }
    private int parseIntSafe(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }
    private float parseFloatSafe(String s) {
        try { return Float.parseFloat(s); } catch (Exception e) { return 0f; }
    }
    private LocalDate parseDateSafe(String s) {
        try { return LocalDate.parse(s); } catch (Exception e) { return null; }
    }
}
