package org.jabref.gui.theme;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

import org.jabref.gui.JabRefFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class StyleSheet {

    static final String DATA_URL_PREFIX = "data:text/css;charset=utf-8;base64,";
    static final URL LIGHT_SCENE_ADDITIONAL_CSS = JabRefFrame.class.getResource("Light.css");
    static final String EMPTY_WEBENGINE_CSS = DATA_URL_PREFIX;

    static final Logger LOGGER = LoggerFactory.getLogger(StyleSheet.class);

    abstract URL getSceneStylesheet();

    abstract String getWebEngineStylesheet();

    Path getWatchPath() {
        return null;
    }

    abstract void reload();

    static StyleSheet create(String name) {
        URL url = JabRefFrame.class.getResource(name);
        if (url == null) {
            try {
                url = Path.of(name).toUri().toURL();
            } catch (InvalidPathException e) {
                LOGGER.warn("Cannot load additional css {} because it is an invalid path: {}", name, e.getLocalizedMessage());
                url = null;
            } catch (MalformedURLException e) {
                LOGGER.warn("Cannot load additional css url {} because it is a malformed url: {}", name, e.getLocalizedMessage());
                url = null;
            }
        }

        /*
            TODO: embedding CSS in a data URL is only desirable in file URLs, as protection against the file being
             removed. This is built into StyleSheetFile (see StyleSheetFile.MAX_IN_MEMORY_CSS_LENGTH for details on
             caching). However, there is a bug in OpenJFX, in that WebEngine does not recognise jrt URLs (modular java
             runtime URLs). This is detailed in https://bugs.openjdk.java.net/browse/JDK-8240969.
             When we upgrade to OpenJFX 16, we no longer need to wrap built in themes as data URLs. Note that we
             already do not wrap the base stylesheet, because it is not used to style the WebEngine. WebEngine is
             used for the Preview Viewer and this does not use the base CSS. This is why we already use StyleSheetResource
             for Base.css.
        */

        if (url == null) {
            return new StyleSheetDataUrl(LIGHT_SCENE_ADDITIONAL_CSS);
        } else if ("file".equals(url.getProtocol())) {
            return new StyleSheetFile(url);
        } else {
            if ("Base.css".equals(name)) {
                return new StyleSheetResource(url);
            } else {
                return new StyleSheetDataUrl(url);
            }
        }
    }

    @Override
    public String toString() {
        return "StyleSheet{" + getSceneStylesheet() + "}";
    }
}
