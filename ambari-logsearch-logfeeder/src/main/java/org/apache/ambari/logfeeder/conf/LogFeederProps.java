/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ambari.logfeeder.conf;

import org.apache.ambari.logfeeder.common.LogFeederConstants;
import org.apache.ambari.logfeeder.conf.output.HdfsOutputConfig;
import org.apache.ambari.logfeeder.conf.output.RolloverConfig;
import org.apache.ambari.logfeeder.conf.output.S3OutputConfig;
import org.apache.ambari.logfeeder.plugin.common.LogFeederProperties;
import org.apache.ambari.logsearch.config.api.LogSearchPropertyDescription;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.support.ResourcePropertySource;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

@Configuration
public class LogFeederProps implements LogFeederProperties {

  @Inject
  private Environment env;

  @Inject
  private RolloverConfig rolloverConfig;

  @Inject
  private S3OutputConfig s3OutputConfig;

  @Inject
  private HdfsOutputConfig hdfsOutputConfig;

  private Properties properties;

  @LogSearchPropertyDescription(
    name = LogFeederConstants.CLUSTER_NAME_PROPERTY,
    description = "The name of the cluster the Log Feeder program runs in.",
    examples = {"cl1"},
    sources = {LogFeederConstants.LOGFEEDER_PROPERTIES_FILE}
  )
  @Value("#{'${" + LogFeederConstants.CLUSTER_NAME_PROPERTY + "}'.toLowerCase()}")
  private String clusterName;

  @LogSearchPropertyDescription(
    name = LogFeederConstants.TMP_DIR_PROPERTY,
    description = "The tmp dir used for creating temporary files.",
    examples = {"/tmp/"},
    defaultValue = "java.io.tmpdir",
    sources = {LogFeederConstants.LOGFEEDER_PROPERTIES_FILE}
  )
  @Value("${"+ LogFeederConstants.TMP_DIR_PROPERTY + ":#{systemProperties['java.io.tmpdir']}}")
  private String tmpDir;

  @LogSearchPropertyDescription(
    name = LogFeederConstants.LOG_FILTER_ENABLE_PROPERTY,
    description = "Enables the filtering of the log entries by log level filters.",
    examples = {"true"},
    defaultValue = "false",
    sources = {LogFeederConstants.LOGFEEDER_PROPERTIES_FILE}
  )
  @Value("${"+ LogFeederConstants.LOG_FILTER_ENABLE_PROPERTY + "}")
  private boolean logLevelFilterEnabled;

  @LogSearchPropertyDescription(
    name = LogFeederConstants.SOLR_IMPLICIT_ROUTING_PROPERTY,
    description = "Use implicit routing for Solr Collections.",
    examples = {"true"},
    defaultValue = "false",
    sources = {LogFeederConstants.LOGFEEDER_PROPERTIES_FILE}
  )
  @Value("${"+ LogFeederConstants.SOLR_IMPLICIT_ROUTING_PROPERTY + ":false}")
  private boolean solrImplicitRouting;

  @LogSearchPropertyDescription(
    name = LogFeederConstants.INCLUDE_DEFAULT_LEVEL_PROPERTY,
    description = "Comma separated list of the default log levels to be enabled by the filtering.",
    examples = {"FATAL,ERROR,WARN"},
    sources = {LogFeederConstants.LOGFEEDER_PROPERTIES_FILE}
  )
  @Value("#{'${" + LogFeederConstants.INCLUDE_DEFAULT_LEVEL_PROPERTY + ":}'.split(',')}")
  private List<String> includeDefaultLogLevels;

  @LogSearchPropertyDescription(
    name = LogFeederConstants.CONFIG_DIR_PROPERTY,
    description = "The directory where shipper configuration files are looked for.",
    examples = {"/usr/lib/ambari-logsearch-logfeeder/conf"},
    defaultValue = "/usr/lib/ambari-logsearch-logfeeder/conf",
    sources = {LogFeederConstants.LOGFEEDER_PROPERTIES_FILE}
  )
  @Value("${"+ LogFeederConstants.CONFIG_DIR_PROPERTY + ":/usr/lib/ambari-logsearch-logfeeder/conf}")
  private String confDir;

  @LogSearchPropertyDescription(
    name = LogFeederConstants.CONFIG_FILES_PROPERTY,
    description = "Comma separated list of the config files containing global / output configurations.",
    examples = {"global.json,output.json", "/usr/lib/ambari-logsearch-logfeeder/conf/global.config.json"},
    sources = {LogFeederConstants.LOGFEEDER_PROPERTIES_FILE}
  )
  @Value("${"+ LogFeederConstants.CONFIG_FILES_PROPERTY + ":}")
  private String configFiles;

  @LogSearchPropertyDescription(
    name = LogFeederConstants.CHECKPOINT_EXTENSION_PROPERTY,
    description = "The extension used for checkpoint files.",
    examples = {"ckp"},
    defaultValue = LogFeederConstants.DEFAULT_CHECKPOINT_EXTENSION,
    sources = {LogFeederConstants.LOGFEEDER_PROPERTIES_FILE}
  )
  @Value("${" + LogFeederConstants.CHECKPOINT_EXTENSION_PROPERTY + ":" + LogFeederConstants.DEFAULT_CHECKPOINT_EXTENSION + "}")
  private String checkPointExtension;

  @LogSearchPropertyDescription(
    name = LogFeederConstants.CHECKPOINT_FOLDER_PROPERTY,
    description = "The folder where checkpoint files are stored.",
    examples = {"/usr/lib/ambari-logsearch-logfeeder/conf/checkpoints"},
    sources = {LogFeederConstants.LOGFEEDER_PROPERTIES_FILE}
  )
  @Value("${" + LogFeederConstants.CHECKPOINT_FOLDER_PROPERTY + ":/usr/lib/ambari-logsearch-logfeeder/conf/checkpoints}")
  public String checkpointFolder;

  @LogSearchPropertyDescription(
    name = LogFeederConstants.DOCKER_CONTAINER_REGISTRY_ENABLED_PROPERTY,
    description = "Enable to monitor docker containers and store their metadata in an in-memory registry.",
    examples = {"true"},
    defaultValue = LogFeederConstants.DOCKER_CONTAINER_REGISTRY_ENABLED_DEFAULT + "",
    sources = {LogFeederConstants.LOGFEEDER_PROPERTIES_FILE}
  )
  @Value("${" + LogFeederConstants.DOCKER_CONTAINER_REGISTRY_ENABLED_PROPERTY + ":false}")
  public boolean dockerContainerRegistryEnabled;

  @LogSearchPropertyDescription(
    name = LogFeederConstants.USE_LOCAL_CONFIGS_PROPERTY,
    description = "Monitor local input.config-*.json files (do not upload them to zookeeper or solr)",
    examples = {"true"},
    defaultValue = LogFeederConstants.USE_LOCAL_CONFIGS_DEFAULT + "",
    sources = {LogFeederConstants.LOGFEEDER_PROPERTIES_FILE}
  )
  @Value("${" + LogFeederConstants.USE_LOCAL_CONFIGS_PROPERTY + ":" + LogFeederConstants.USE_LOCAL_CONFIGS_DEFAULT +"}")
  public boolean useLocalConfigs;

  @LogSearchPropertyDescription(
    name = LogFeederConstants.USE_SOLR_FILTER_STORAGE_PROPERTY,
    description = "Use solr as a log level filter storage",
    examples = {"true"},
    defaultValue = LogFeederConstants.USE_SOLR_FILTER_STORAGE_DEFAULT + "",
    sources = {LogFeederConstants.LOGFEEDER_PROPERTIES_FILE}
  )
  @Value("${" + LogFeederConstants.USE_SOLR_FILTER_STORAGE_PROPERTY + ":" + LogFeederConstants.USE_SOLR_FILTER_STORAGE_DEFAULT +"}")
  public boolean solrFilterStorage;

  @LogSearchPropertyDescription(
    name = LogFeederConstants.USE_ZK_FILTER_STORAGE_PROPERTY,
    description = "Use zk as a log level filter storage (works only with local config)",
    examples = {"true"},
    defaultValue = LogFeederConstants.USE_ZK_FILTER_STORAGE_DEFAULT + "",
    sources = {LogFeederConstants.LOGFEEDER_PROPERTIES_FILE}
  )
  @Value("${" + LogFeederConstants.USE_ZK_FILTER_STORAGE_PROPERTY + ":" + LogFeederConstants.USE_ZK_FILTER_STORAGE_DEFAULT +"}")
  public boolean zkFilterStorage;

  @LogSearchPropertyDescription(
    name = LogFeederConstants.MONITOR_SOLR_FILTER_STORAGE_PROPERTY,
    description = "Monitor log level filters (in solr) periodically - used for checking updates.",
    examples = {"false"},
    defaultValue = LogFeederConstants.MONITOR_SOLR_FILTER_STORAGE_DEFAULT + "",
    sources = {LogFeederConstants.LOGFEEDER_PROPERTIES_FILE}
  )
  @Value("${" + LogFeederConstants.MONITOR_SOLR_FILTER_STORAGE_PROPERTY + ":" + LogFeederConstants.MONITOR_SOLR_FILTER_STORAGE_DEFAULT +"}")
  public boolean solrFilterMonitor;

  @LogSearchPropertyDescription(
    name = LogFeederConstants.MONITOR_SOLR_FILTER_INTERVAL_PROPERTY,
    description = "Time interval (in seconds) between monitoring input config filter definitions from Solr.",
    examples = {"60"},
    defaultValue = "30",
    sources = {LogFeederConstants.LOGFEEDER_PROPERTIES_FILE}
  )
  @Value("${" + LogFeederConstants.MONITOR_SOLR_FILTER_INTERVAL_PROPERTY + ":30}")
  public Integer solrFilterMonitorInterval;

  @LogSearchPropertyDescription(
    name = LogFeederConstants.SOLR_ZK_CONNECTION_STRING,
    description = "Zookeeper connection string for Solr.",
    examples = {"localhost1:2181,localhost2:2181/mysolr_znode"},
    sources = {LogFeederConstants.LOGFEEDER_PROPERTIES_FILE}
  )
  @Value("${" + LogFeederConstants.SOLR_ZK_CONNECTION_STRING + ":}")
  private String solrZkConnectString;

  @LogSearchPropertyDescription(
    name = LogFeederConstants.SOLR_URLS,
    description = "Comma separated solr urls (with protocol and port), override "+ LogFeederConstants.SOLR_ZK_CONNECTION_STRING + " config",
    examples = {"https://localhost1:8983/solr,https://localhost2:8983"},
    sources = {LogFeederConstants.LOGFEEDER_PROPERTIES_FILE}
  )
  @Value("${" + LogFeederConstants.SOLR_URLS + ":}")
  private String solrUrlsStr;

  @LogSearchPropertyDescription(
    name = LogFeederConstants.SOLR_CLOUD_DISCOVER,
    description = "On startup, with a Solr Cloud client, the Solr nodes will be discovered, then LBHttpClient will be built from that.",
    examples = {"true"},
    sources = {LogFeederConstants.LOGFEEDER_PROPERTIES_FILE},
    defaultValue = "false"
  )
  @Value("${" + LogFeederConstants.SOLR_CLOUD_DISCOVER + ":false}")
  private boolean solrCloudDiscover;

  @LogSearchPropertyDescription(
    name = LogFeederConstants.SOLR_METADATA_COLLECTION,
    description = "Metadata collection name that could contain log level filters or input configurations.",
    examples = {"logsearch_metadata"},
    sources = {LogFeederConstants.LOGFEEDER_PROPERTIES_FILE}
  )
  @Value("${" + LogFeederConstants.SOLR_METADATA_COLLECTION + ":logsearch_metadata}")
  private String solrMetadataCollection;

  @LogSearchPropertyDescription(
    name = LogFeederConstants.CLOUD_STORAGE_MODE,
    description = "Option to support sending logs to cloud storage. You can choose between supporting only cloud storage, non-cloud storage or both",
    examples = {"default", "cloud", "hybrid"},
    defaultValue = "default",
    sources = {LogFeederConstants.LOGFEEDER_PROPERTIES_FILE}
  )
  @Value("${" + LogFeederConstants.CLOUD_STORAGE_MODE + ":default}")
  public LogFeederMode cloudStorageMode;

  @LogSearchPropertyDescription(
    name = LogFeederConstants.CLOUD_STORAGE_DESTINATION,
    description = "Type of storage that is the destination for cloud output logs.",
    examples = {"hdfs", "s3"},
    defaultValue = "none",
    sources = {LogFeederConstants.LOGFEEDER_PROPERTIES_FILE}
  )
  @Value("${" + LogFeederConstants.CLOUD_STORAGE_DESTINATION + ":none}")
  private CloudStorageDestination cloudStorageDestination;

  @LogSearchPropertyDescription(
    name = LogFeederConstants.CLOUD_STORAGE_UPLOAD_ON_SHUTDOWN,
    description = "Try to upload archived files on shutdown",
    examples = {"true"},
    defaultValue = "false",
    sources = {LogFeederConstants.LOGFEEDER_PROPERTIES_FILE}
  )
  @Value("${" + LogFeederConstants.CLOUD_STORAGE_UPLOAD_ON_SHUTDOWN + ":false}")
  private boolean cloudStorageUploadOnShutdown;

  @LogSearchPropertyDescription(
    name = LogFeederConstants.CLOUD_STORAGE_UPLOADER_INTERVAL_SECONDS,
    description = "Second interval, that is used to check against there are any files to upload to cloud storage or not.",
    examples = {"10"},
    defaultValue = "60",
    sources = {LogFeederConstants.LOGFEEDER_PROPERTIES_FILE}
  )
  @Value("${" + LogFeederConstants.CLOUD_STORAGE_UPLOADER_INTERVAL_SECONDS + ":60}")
  private Integer cloudStorageUploaderIntervalSeconds;

  @LogSearchPropertyDescription(
    name = LogFeederConstants.CLOUD_STORAGE_UPLOADER_TIMEOUT_MINUTUES,
    description = "Timeout value for uploading task to cloud storage in minutes.",
    examples = {"10"},
    defaultValue = "60",
    sources = {LogFeederConstants.LOGFEEDER_PROPERTIES_FILE}
  )
  @Value("${" + LogFeederConstants.CLOUD_STORAGE_UPLOADER_TIMEOUT_MINUTUES + ":60}")
  private Integer cloudStorageUploaderTimeoutMinutes;

  @LogSearchPropertyDescription(
    name = LogFeederConstants.CLOUD_STORAGE_USE_HDFS_CLIENT,
    description = "Use hdfs client with cloud connectors instead of the core clients for shipping data to cloud storage",
    examples = {"true"},
    defaultValue = "false",
    sources = {LogFeederConstants.LOGFEEDER_PROPERTIES_FILE}
  )
  @Value("${" + LogFeederConstants.CLOUD_STORAGE_USE_HDFS_CLIENT + ":true}")
  private boolean useCloudHdfsClient;

  @LogSearchPropertyDescription(
    name = LogFeederConstants.CLOUD_STORAGE_CUSTOM_FS,
    description = "If it is not empty, override fs.defaultFS for HDFS client. Can be useful to write data to a different bucket (from other services) if the bucket address is read from core-site.xml",
    examples = {"s3a://anotherbucket"},
    sources = {LogFeederConstants.LOGFEEDER_PROPERTIES_FILE}
  )
  @Value("${" + LogFeederConstants.CLOUD_STORAGE_CUSTOM_FS + ":}")
  private String customFs;

  @LogSearchPropertyDescription(
    name = LogFeederConstants.CLOUD_STORAGE_BASE_PATH,
    description = "Base path prefix for storing logs (cloud storage / hdfs), could be an absolute path or URI. (if URI used, that will override the default.FS with HDFS client)",
    examples = {"/user/logsearch/mypath", "s3a:///user/logsearch"},
    defaultValue = "/apps/logsearch",
    sources = {LogFeederConstants.LOGFEEDER_PROPERTIES_FILE}
  )
  @Value("${" + LogFeederConstants.CLOUD_STORAGE_BASE_PATH + ":/apps/logsearch}")
  private String cloudBasePath;

  @LogSearchPropertyDescription(
    name = LogFeederConstants.CLOUD_STORAGE_USE_FILTERS,
    description = "Use filters for inputs (with filters the output format will be JSON)",
    examples = {"true"},
    defaultValue = "false",
    sources = {LogFeederConstants.LOGFEEDER_PROPERTIES_FILE}
  )
  @Value("${" + LogFeederConstants.CLOUD_STORAGE_USE_FILTERS + ":false}")
  private boolean cloudStorageUseFilters;

  @Inject
  private LogEntryCacheConfig logEntryCacheConfig;

  @Inject
  private InputSimulateConfig inputSimulateConfig;

  @Inject
  private LogFeederSecurityConfig logFeederSecurityConfig;

  public String getClusterName() {
    return clusterName;
  }

  public void setClusterName(String clusterName) {
    this.clusterName = clusterName;
  }

  public Properties getProperties() {
    return properties;
  }

  public String getTmpDir() {
    return tmpDir;
  }

  public boolean isLogLevelFilterEnabled() {
    return logLevelFilterEnabled;
  }

  public List<String> getIncludeDefaultLogLevels() {
    return includeDefaultLogLevels;
  }

  public String getConfDir() {
    return confDir;
  }

  public void setConfDir(String confDir) {
    this.confDir = confDir;
  }

  public String getConfigFiles() {
    return configFiles;
  }

  public void setConfigFiles(String configFiles) {
    this.configFiles = configFiles;
  }

  public LogEntryCacheConfig getLogEntryCacheConfig() {
    return logEntryCacheConfig;
  }

  public void setLogEntryCacheConfig(LogEntryCacheConfig logEntryCacheConfig) {
    this.logEntryCacheConfig = logEntryCacheConfig;
  }

  public InputSimulateConfig getInputSimulateConfig() {
    return inputSimulateConfig;
  }

  public void setInputSimulateConfig(InputSimulateConfig inputSimulateConfig) {
    this.inputSimulateConfig = inputSimulateConfig;
  }

  public LogFeederSecurityConfig getLogFeederSecurityConfig() {
    return logFeederSecurityConfig;
  }

  public void setLogFeederSecurityConfig(LogFeederSecurityConfig logFeederSecurityConfig) {
    this.logFeederSecurityConfig = logFeederSecurityConfig;
  }

  public String getCheckPointExtension() {
    return checkPointExtension;
  }

  public void setCheckPointExtension(String checkPointExtension) {
    this.checkPointExtension = checkPointExtension;
  }

  public String getCheckpointFolder() {
    return checkpointFolder;
  }

  public void setCheckpointFolder(String checkpointFolder) {
    this.checkpointFolder = checkpointFolder;
  }

  public boolean isSolrImplicitRouting() {
    return solrImplicitRouting;
  }

  public void setSolrImplicitRouting(boolean solrImplicitRouting) {
    this.solrImplicitRouting = solrImplicitRouting;
  }

  public boolean isDockerContainerRegistryEnabled() {
    return dockerContainerRegistryEnabled;
  }

  public void setDockerContainerRegistryEnabled(boolean dockerContainerRegistryEnabled) {
    this.dockerContainerRegistryEnabled = dockerContainerRegistryEnabled;
  }

  public boolean isUseLocalConfigs() {
    return this.useLocalConfigs;
  }

  public void setUseLocalConfigs(boolean useLocalConfigs) {
    this.useLocalConfigs = useLocalConfigs;
  }

  public boolean isSolrFilterStorage() {
    return solrFilterStorage;
  }

  public void setSolrFilterStorage(boolean solrFilterStorage) {
    this.solrFilterStorage = solrFilterStorage;
  }

  public String getSolrZkConnectString() {
    return solrZkConnectString;
  }

  public void setSolrZkConnectString(String solrZkConnectString) {
    this.solrZkConnectString = solrZkConnectString;
  }

  public boolean isSolrFilterMonitor() {
    return solrFilterMonitor;
  }

  public void setSolrFilterMonitor(boolean solrFilterMonitor) {
    this.solrFilterMonitor = solrFilterMonitor;
  }

  public Integer getSolrFilterMonitorInterval() {
    return solrFilterMonitorInterval;
  }

  public void setSolrFilterMonitorInterval(Integer solrFilterMonitorInterval) {
    this.solrFilterMonitorInterval = solrFilterMonitorInterval;
  }

  public String getSolrUrlsStr() {
    return this.solrUrlsStr;
  }

  public void setSolrUrlsStr(String solrUrlsStr) {
    this.solrUrlsStr = solrUrlsStr;
  }

  public boolean isZkFilterStorage() {
    return zkFilterStorage;
  }

  public void setZkFilterStorage(boolean zkFilterStorage) {
    this.zkFilterStorage = zkFilterStorage;
  }

  public LogFeederMode getCloudStorageMode() {
    return cloudStorageMode;
  }

  public void setCloudStorageMode(LogFeederMode cloudStorageMode) {
    this.cloudStorageMode = cloudStorageMode;
  }

  public HdfsOutputConfig getHdfsOutputConfig() {
    return hdfsOutputConfig;
  }

  public S3OutputConfig getS3OutputConfig() {
    return s3OutputConfig;
  }

  public void setS3OutputConfig(S3OutputConfig s3OutputConfig) {
    this.s3OutputConfig = s3OutputConfig;
  }

  public RolloverConfig getRolloverConfig() {
    return rolloverConfig;
  }

  public void setRolloverConfig(RolloverConfig rolloverConfig) {
    this.rolloverConfig = rolloverConfig;
  }

  public void setHdfsOutputConfig(HdfsOutputConfig hdfsOutputConfig) {
    this.hdfsOutputConfig = hdfsOutputConfig;
  }

  public CloudStorageDestination getCloudStorageDestination() {
    return cloudStorageDestination;
  }

  public void setCloudStorageDestination(CloudStorageDestination cloudStorageDestination) {
    this.cloudStorageDestination = cloudStorageDestination;
  }

  public boolean isCloudStorageUploadOnShutdown() {
    return cloudStorageUploadOnShutdown;
  }

  public void setCloudStorageUploadOnShutdown(boolean cloudStorageUploadOnShutdown) {
    this.cloudStorageUploadOnShutdown = cloudStorageUploadOnShutdown;
  }

  public Integer getCloudStorageUploaderIntervalSeconds() {
    return cloudStorageUploaderIntervalSeconds;
  }

  public void setCloudStorageUploaderIntervalSeconds(Integer cloudStorageUploaderIntervalSeconds) {
    this.cloudStorageUploaderIntervalSeconds = cloudStorageUploaderIntervalSeconds;
  }

  public Integer getCloudStorageUploaderTimeoutMinutes() {
    return cloudStorageUploaderTimeoutMinutes;
  }

  public void setCloudStorageUploaderTimeoutMinutes(Integer cloudStorageUploaderTimeoutMinutes) {
    this.cloudStorageUploaderTimeoutMinutes = cloudStorageUploaderTimeoutMinutes;
  }

  public boolean isUseCloudHdfsClient() {
    return useCloudHdfsClient;
  }

  public void setUseCloudHdfsClient(boolean useCloudHdfsClient) {
    this.useCloudHdfsClient = useCloudHdfsClient;
  }

  public String getCustomFs() {
    return customFs;
  }

  public void setCustomFs(String customFs) {
    this.customFs = customFs;
  }

  public boolean isCloudStorageUseFilters() {
    return cloudStorageUseFilters;
  }

  public void setCloudStorageUseFilters(boolean cloudStorageUseFilters) {
    this.cloudStorageUseFilters = cloudStorageUseFilters;
  }

  public String getCloudBasePath() {
    return cloudBasePath;
  }

  public void setCloudBasePath(String cloudBasePath) {
    this.cloudBasePath = cloudBasePath;
  }

  public String getSolrMetadataCollection() {
    return solrMetadataCollection;
  }

  public void setSolrMetadataCollection(String solrMetadataCollection) {
    this.solrMetadataCollection = solrMetadataCollection;
  }

  public boolean isSolrCloudDiscover() {
    return solrCloudDiscover;
  }

  public void setSolrCloudDiscover(boolean solrCloudDiscover) {
    this.solrCloudDiscover = solrCloudDiscover;
  }

  public String[] getSolrUrls() {
    if (StringUtils.isNotBlank(this.solrUrlsStr)) {
      return this.solrUrlsStr.split(",");
    }
    return null;
  }

  @PostConstruct
  public void init() {
    properties = new Properties();
    MutablePropertySources propSrcs = ((AbstractEnvironment) env).getPropertySources();
    ResourcePropertySource propertySource = (ResourcePropertySource) propSrcs.get("class path resource [" +
      LogFeederConstants.LOGFEEDER_PROPERTIES_FILE + "]");
    if (propertySource != null) {
      Stream.of(propertySource)
        .map(MapPropertySource::getPropertyNames)
        .flatMap(Arrays::<String>stream)
        .forEach(propName -> properties.setProperty(propName, env.getProperty(propName)));
    } else {
      throw new IllegalArgumentException("Cannot find logfeeder.properties on the classpath");
    }
  }
}
