/*
 * Copyright 2022 https://dejvokep.dev/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.dejvokep.boostedyaml.settings.updater;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import dev.dejvokep.boostedyaml.route.RouteFactory;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.dvs.Pattern;
import dev.dejvokep.boostedyaml.dvs.versioning.AutomaticVersioning;
import dev.dejvokep.boostedyaml.dvs.versioning.ManualVersioning;
import dev.dejvokep.boostedyaml.dvs.versioning.Versioning;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Updater settings cover all options related explicitly (only) to file updating.
 * <p>
 * To start using this library, it is recommended to take a look at the following methods:
 * <ul>
 *     <li>{@link Builder#setAutoSave(boolean)}</li>
 *     <li>{@link Builder#setVersioning(Versioning)}</li>
 * </ul>
 */
@SuppressWarnings("unused")
public class UpdaterSettings {

    /**
     * If to, by default, automatically save the file after updating.
     */
    public static final boolean DEFAULT_AUTO_SAVE = true;
    /**
     * If to enable file downgrading by default.
     */
    public static final boolean DEFAULT_ENABLE_DOWNGRADING = true;
    /**
     * If to keep all non-merged content (present in the document, but not in the defaults) inside the document by
     * default.
     */
    public static final boolean DEFAULT_KEEP_ALL = false;
    /**
     * Default merge preservation rules.
     */
    public static final Map<MergeRule, Boolean> DEFAULT_MERGE_RULES = Collections.unmodifiableMap(new HashMap<MergeRule, Boolean>() {{
        put(MergeRule.MAPPINGS, true);
        put(MergeRule.MAPPING_AT_SECTION, false);
        put(MergeRule.SECTION_AT_MAPPING, false);
    }});
    /**
     * Default versioning.
     */
    public static final Versioning DEFAULT_VERSIONING = null;

    /**
     * Default updater settings.
     */
    public static final UpdaterSettings DEFAULT = builder().build();

    //Save automatically
    private final boolean autoSave;
    //Enable downgrading
    private final boolean enableDowngrading;
    //Keep all contents
    private final boolean keepAll;
    //Merge rules
    private final Map<MergeRule, Boolean> mergeRules;
    //Routes to ignore
    private final Map<String, Set<Route>> ignored;
    private final Map<String, Set<String>> stringIgnored;
    //Relocations
    private final Map<String, Map<Route, Route>> relocations;
    private final Map<String, Map<String, String>> stringRelocations;
    //Versioning
    private final Versioning versioning;

    /**
     * Creates final, immutable updater settings from the given builder.
     *
     * @param builder the builder
     */
    public UpdaterSettings(Builder builder) {
        this.autoSave = builder.autoSave;
        this.enableDowngrading = builder.enableDowngrading;
        this.keepAll = builder.keepAll;
        this.mergeRules = builder.mergeRules;
        this.ignored = builder.ignored;
        this.stringIgnored = builder.stringIgnored;
        this.relocations = builder.relocations;
        this.stringRelocations = builder.stringRelocations;
        this.versioning = builder.versioning;
    }

    /**
     * Returns merge preservation rules.
     * <p>
     * The given map contains the merge rule as the key, with value representing if to preserve content already in the
     * document instead of the equivalent from defaults.
     *
     * @return the merge rules
     */
    public Map<MergeRule, Boolean> getMergeRules() {
        return mergeRules;
    }

    /**
     * Returns which blocks (represented by their routes) to ignore (including their contents) for the version ID.
     * Merges the string-based ignored routes.
     *
     * @param versionId version for which to return the ignored routes
     * @param separator separator to split string based routes by
     * @return routes representing blocks to ignore, per version ID
     */
    public Set<Route> getIgnored(@NotNull String versionId, char separator) {
        //Set
        Set<Route> ignored = new HashSet<>(this.ignored.getOrDefault(versionId, Collections.emptySet()));

        //If string relocations are defined
        if (stringIgnored.containsKey(versionId)) {
            //Create factory
            RouteFactory factory = new RouteFactory(separator);
            //All entries
            for (String route : stringIgnored.get(versionId))
                ignored.add(factory.create(route));
        }

        //Return
        return ignored;
    }

    /**
     * Returns relocations (in <code>from route = to route</code> format) for the version ID. Merges the string-based
     * relocations.
     *
     * @param versionId version for which to return the relocations
     * @param separator separator to split string based relocation routes by
     * @return the relocations
     */
    public Map<Route, Route> getRelocations(@NotNull String versionId, char separator) {
        //Map
        Map<Route, Route> relocations = new HashMap<>(this.relocations.getOrDefault(versionId, Collections.emptyNavigableMap()));
        //If string relocations are defined
        if (stringRelocations.containsKey(versionId)) {
            //Create factory
            RouteFactory factory = new RouteFactory(separator);
            //Add all
            for (Map.Entry<String, String> entry : stringRelocations.get(versionId).entrySet())
                relocations.computeIfAbsent(factory.create(entry.getKey()), route -> factory.create(entry.getValue()));
        }

        return relocations;
    }

    /**
     * Returns versioning information.
     *
     * @return the versioning
     */
    public Versioning getVersioning() {
        return versioning;
    }

    /**
     * Returns if to enable downgrading.
     *
     * @return if to enable downgrading
     */
    public boolean isEnableDowngrading() {
        return enableDowngrading;
    }

    /**
     * Returns if to keep all non-merged contents of the document.
     *
     * @return if to keep all non-merged document contents
     */
    public boolean isKeepAll() {
        return keepAll;
    }

    /**
     * Returns if the file should automatically be saved after finished updating (does not save if nothing's changed).
     *
     * @return if to save automatically after update
     */
    public boolean isAutoSave() {
        return autoSave;
    }

    /**
     * Returns a new builder.
     *
     * @return the new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns a new builder with the same configuration as the given settings.
     *
     * @param settings preset settings
     * @return the new builder
     */
    public static Builder builder(UpdaterSettings settings) {
        return builder()
                .setAutoSave(settings.autoSave)
                .setEnableDowngrading(settings.enableDowngrading)
                .setKeepAll(settings.keepAll)
                .setMergeRules(settings.mergeRules)
                .setIgnoredRoutes(settings.ignored)
                .setIgnoredStringRoutes(settings.stringIgnored)
                .setRelocations(settings.relocations)
                .setStringRelocations(settings.stringRelocations)
                .setVersioning(settings.versioning);
    }

    /**
     * Builder for updater settings.
     */
    public static class Builder {

        //Save automatically
        private boolean autoSave = DEFAULT_AUTO_SAVE;
        //Enable downgrading
        private boolean enableDowngrading = DEFAULT_ENABLE_DOWNGRADING;
        //Keep all contents
        private boolean keepAll = DEFAULT_KEEP_ALL;
        //Merge rules
        private final Map<MergeRule, Boolean> mergeRules = new HashMap<>(DEFAULT_MERGE_RULES);
        //Routes to ignore
        private final Map<String, Set<Route>> ignored = new HashMap<>();
        private final Map<String, Set<String>> stringIgnored = new HashMap<>();
        //Relocations
        private final Map<String, Map<Route, Route>> relocations = new HashMap<>();
        private final Map<String, Map<String, String>> stringRelocations = new HashMap<>();
        //Versioning
        private Versioning versioning = DEFAULT_VERSIONING;

        /**
         * Creates a new builder will all the default settings applied.
         */
        private Builder() {
        }

        /**
         * Sets if the file should automatically be saved using {@link YamlDocument#save()} after the updater has
         * finished updating (does not save if nothing's changed).
         * <p>
         * Not effective if there is no {@link YamlDocument#getFile() file associated} with the document.
         * <p>
         * <b>Default: </b>{@link #DEFAULT_AUTO_SAVE}
         *
         * @param autoSave if to save automatically after update
         * @return the builder
         */
        public Builder setAutoSave(boolean autoSave) {
            this.autoSave = autoSave;
            return this;
        }

        /**
         * Sets if to enable downgrading.
         * <p>
         * <b>Downgrading is considered to be a situation:</b>
         * <ul>
         *     <li>when there are valid version IDs found for both files (supplied manually or automatically from files),</li>
         *     <li>the version ID of the document represents newer version than version ID of the defaults.</li>
         * </ul>
         * Please note that by specification, the default file has to have a valid ID supplied/specified.
         * <p>
         * That means, if no versioning is supplied, if the version ID of the updated document was not found (automatic DVS) or
         * not supplied (manual DVS), or the ID of the document is not parsable by the given pattern, this setting is not effective.
         * <p>
         * If enabled and the updater detects downgrading, the updater will skip relocations, proceeding directly to merging. If disabled, throws an error if downgrading. If configured like so, you may also want to disable
         * {@link LoaderSettings.Builder#setAutoUpdate(boolean)} (if an error is thrown, you won't be able to initialize the file - update manually).
         * <p>
         * <b>Default: </b>{@link #DEFAULT_ENABLE_DOWNGRADING}
         *
         * @param enableDowngrading if to enable downgrading
         * @return the builder
         */
        public Builder setEnableDowngrading(boolean enableDowngrading) {
            this.enableDowngrading = enableDowngrading;
            return this;
        }

        /**
         * Sets if to keep all non-merged (they don't have equivalent in the defaults) blocks of the document.
         * <p>
         * <b>Default: </b>{@link #DEFAULT_KEEP_ALL}
         *
         * @param keepAll if to keep all non-merged blocks of the document
         * @return the builder
         */
        public Builder setKeepAll(boolean keepAll) {
            this.keepAll = keepAll;
            return this;
        }

        /**
         * Sets merge preservation rules. Overwrites only rules that are defined in the given map. You can learn more at
         * {@link MergeRule}.
         * <p>
         * The given map should contain the merge rule as the key, with value representing if to preserve content
         * already in the document instead of the equivalent from defaults.
         * <p>
         * <b>Default: </b>{@link #DEFAULT_MERGE_RULES}
         *
         * @param mergeRules the merge rules to set
         * @return the builder
         * @see #setMergeRule(MergeRule, boolean)
         */
        public Builder setMergeRules(@NotNull Map<MergeRule, Boolean> mergeRules) {
            this.mergeRules.putAll(mergeRules);
            return this;
        }

        /**
         * Sets merge preservation rule and overwrites the already existing value for the given rule. You can learn more
         * at {@link MergeRule}.
         * <p>
         * <b>Default: </b>{@link #DEFAULT_MERGE_RULES}
         *
         * @param rule             the rule to set
         * @param preserveDocument if to preserve content already in the document instead of the equivalent from
         *                         defaults for this rule
         * @return the builder
         */
        public Builder setMergeRule(@NotNull MergeRule rule, boolean preserveDocument) {
            this.mergeRules.put(rule, preserveDocument);
            return this;
        }

        /**
         * Sets which blocks (represented by their routes) to ignore (including their contents) while updating to a
         * certain version ID. If there already are routes defined for version ID, which is also present in the given
         * map, they are overwritten.
         * <p>
         * <b>This is generally useful for sections which users can freely extend.</b> In this sense we can say that
         * you should specify a version ID of the file and routes of such sections which were in the file with the ID.
         * <p>
         * <b>You must specify a versioning for this setting to be effective.</b>
         *
         * @param routes routes to ignore, per version ID
         * @return the builder
         * @see #setIgnoredRoutes(String, Set)
         */
        public Builder setIgnoredRoutes(@NotNull Map<String, Set<Route>> routes) {
            this.ignored.putAll(routes);
            return this;
        }

        /**
         * Sets which blocks (represented by their routes) to ignore (including their contents) while updating to the
         * specified version ID. If there already are routes defined for the given ID, they are overwritten.
         * <p>
         * <b>This is generally useful for sections which users can freely extend.</b> In this sense we can say that
         * you should specify a version ID of the file and routes of such sections which were in the file with the ID.
         * <p>
         * <b>You must specify a versioning for this setting to be effective.</b>
         *
         * @param versionId the version ID
         * @param routes    the set of routes representing blocks to ignore at the version ID
         * @return the builder
         */
        public Builder setIgnoredRoutes(@NotNull String versionId, @NotNull Set<Route> routes) {
            this.ignored.put(versionId, routes);
            return this;
        }

        /**
         * Sets which blocks (represented by their <i>string</i> routes) to ignore (including their contents) while
         * updating to a certain version ID. If there already are routes defined for version ID, which is also present
         * in the given map, they are overwritten.
         * <p>
         * <b>This is generally useful for sections which users can freely extend.</b> In this sense we can say that
         * you should specify a version ID of the file and routes of such sections which were in the file with the ID.
         * <p>
         * <b>You must specify a versioning for this setting to be effective.</b>
         *
         * @param routes <i>string</i> routes to ignore, per version ID
         * @return the builder
         * @see #setIgnoredStringRoutes(String, Set)
         */
        public Builder setIgnoredStringRoutes(@NotNull Map<String, Set<String>> routes) {
            this.stringIgnored.putAll(routes);
            return this;
        }

        /**
         * Sets which blocks (represented by their <i>string</i> routes) to ignore (including their contents) while
         * updating to a certain version ID. If there already are routes defined for the given ID, they are
         * overwritten.
         * <p>
         * <b>This is generally useful for sections which users can freely extend.</b> In this sense we can say that
         * you should specify a version ID of the file and routes of such sections which were in the file with the ID.
         * <p>
         * <b>You must specify a versioning for this setting to be effective.</b>
         *
         * @param versionId the version ID
         * @param routes    the set of <i>string</i> routes representing blocks to ignore at the version ID
         * @return the builder
         */
        public Builder setIgnoredStringRoutes(@NotNull String versionId, @NotNull Set<String> routes) {
            this.stringIgnored.put(versionId, routes);
            return this;
        }

        /**
         * Sets relocations (in <code>from route = to route</code> format) per version ID, at which they took place. If
         * there already are relocations defined for version ID which is also present in the given map, they are
         * overwritten.
         * <p>
         * <b>Relocations define that some setting was moved from route <i>x</i> to <i>y</i>, enabling the updater to
         * reproduce those steps without any content loss.</b> The ID at which a relocation took effect is equal to ID
         * of the file which included the changes.
         * <p>
         * <b>You must specify a versioning for this setting to be effective.</b>
         *
         * @param relocations the relocations, per version ID
         * @return the builder
         * @see #setRelocations(String, Map)
         */
        public Builder setRelocations(@NotNull Map<String, Map<Route, Route>> relocations) {
            this.relocations.putAll(relocations);
            return this;
        }

        /**
         * Sets relocations (in <code>from route = to route</code> format) that took effect at the given version ID. If
         * there already are relocations defined for the version ID, they are overwritten.
         * <p>
         * <b>Relocations define that some setting was moved from route <i>x</i> to <i>y</i>, enabling the updater to
         * reproduce those steps without any content loss.</b>The ID at which a relocation took effect is equal to ID of
         * the file which included the changes.
         * <p>
         * <b>You must specify a versioning for this setting to be effective.</b>
         *
         * @param versionId   the version ID
         * @param relocations relocations that took effect at the version ID
         * @return the builder
         */
        public Builder setRelocations(@NotNull String versionId, @NotNull Map<Route, Route> relocations) {
            this.relocations.put(versionId, relocations);
            return this;
        }

        /**
         * Sets relocations (in <code>from route = to route</code> format) per version ID, at which they took place. If
         * there already are relocations defined for version ID which is also present in the given map, they are
         * overwritten.
         * <p>
         * <b>Relocations define that some setting was moved from route <i>x</i> to <i>y</i>, enabling the updater to
         * reproduce those steps without any content loss.</b>The ID at which a relocation took effect is equal to ID of
         * the file which included the changes.
         * <p>
         * <b>Please note</b> that all relocations will be merged when updating, with {@link Route}-based relocations
         * having higher priority.
         * <p>
         * <b>You must specify a versioning for this setting to be effective.</b>
         *
         * @param relocations the relocations, per version ID
         * @return the builder
         * @see #setStringRelocations(String, Map)
         */
        public Builder setStringRelocations(@NotNull Map<String, Map<String, String>> relocations) {
            this.stringRelocations.putAll(relocations);
            return this;
        }

        /**
         * Sets relocations (in <code>from route = to route</code> format) that took effect at the given version ID. If
         * there already are relocations defined for the version ID, they are overwritten.
         * <p>
         * <b>Relocations define that some setting was moved from route <i>x</i> to <i>y</i>, enabling the updater to
         * reproduce those steps without any content loss.</b>The ID at which a relocation took effect is equal to ID of
         * the file which included the changes.
         * <p>
         * <b>Please note</b> that all relocations will be merged when updating, with {@link Route}-based relocations
         * having higher priority.
         * <p>
         * <b>You must specify a versioning for this setting to be effective.</b>
         *
         * @param versionId   the version ID
         * @param relocations relocations that took effect at the version ID
         * @return the builder
         */
        public Builder setStringRelocations(@NotNull String versionId, @NotNull Map<String, String> relocations) {
            this.stringRelocations.put(versionId, relocations);
            return this;
        }

        /**
         * Sets versioning information. An {@link IllegalArgumentException} might be thrown during updating process in
         * certain cases (always make sure to read the documentation of the object you are giving).
         *
         * @param versioning the versioning
         * @return the builder
         * @see #setVersioning(Pattern, String, String)
         * @see #setVersioning(Pattern, Route)
         */
        public Builder setVersioning(@NotNull Versioning versioning) {
            this.versioning = versioning;
            return this;
        }

        /**
         * Sets versioning information manually. The given string version IDs must follow the given pattern.
         * <p>
         * If the document version ID is <code>null</code> (e.g. underlying file was created before your plugin started
         * using this library/updater) or it is not valid, it's version will be treated like the oldest one specified by
         * the given pattern (which effectively means all relocations given will be applied to it).
         * <p>
         * If the ID of the defaults couldn't be parsed, expect a {@link NullPointerException} during runtime.
         * <p>
         * <i>You may want to disable {@link LoaderSettings.Builder#setAutoUpdate(boolean)}
         * and rather update manually by calling {@link YamlDocument#update()} (because if an error is thrown, you won't
         * be able to initialize the file).</i>
         *
         * @param pattern           the pattern
         * @param documentVersionId version ID of the document that's being updated
         * @param defaultsVersionId version ID of the defaults
         * @return the builder
         * @see #setVersioning(Versioning)
         */
        public Builder setVersioning(@NotNull Pattern pattern, @Nullable String documentVersionId, @NotNull String defaultsVersionId) {
            return setVersioning(new ManualVersioning(pattern, documentVersionId, defaultsVersionId));
        }

        /**
         * Sets versioning information to be obtained automatically (directly from the documents and defaults).
         * <p>
         * It must be guaranteed that version ID of the defaults is present at the route and is valid (following the
         * pattern), a {@link NullPointerException} will be thrown during the updating process otherwise. If no version
         * ID is found in the document (that's being updated) at the route, or is invalid, the updater will treat its
         * version ID as the oldest specified by the given pattern.
         *
         * @param pattern the pattern
         * @param route   the route to version IDs (of both files)
         * @return the builder
         */
        public Builder setVersioning(@NotNull Pattern pattern, @NotNull Route route) {
            return setVersioning(new AutomaticVersioning(pattern, route));
        }

        /**
         * Sets versioning information to be obtained automatically (directly from the documents and defaults).
         * <p>
         * It must be guaranteed that version ID of the defaults is present at the route and is valid (following the
         * pattern), a {@link NullPointerException} will be thrown during the updating process otherwise. If no version
         * ID is found in the document (that's being updated) at the route, or is invalid, the updater will treat its
         * version ID as the oldest specified by the given pattern.
         *
         * @param pattern the pattern
         * @param route   the route to version IDs (of both files)
         * @return the builder
         */
        public Builder setVersioning(@NotNull Pattern pattern, @NotNull String route) {
            return setVersioning(new AutomaticVersioning(pattern, route));
        }

        /**
         * Builds the settings.
         *
         * @return the settings
         */
        public UpdaterSettings build() {
            return new UpdaterSettings(this);
        }
    }
}