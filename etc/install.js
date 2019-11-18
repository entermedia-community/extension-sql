importPackage( Packages.org.openedit.util );
importPackage( Packages.java.util );
importPackage( Packages.java.lang );
importPackage( Packages.java.io );
importPackage( Packages.org.entermediadb.modules.update );
importPackage( Packages.com.openedit.modules.scheduler );



var name = "extension-sql";

var war = "http://dev.entermediasoftware.com/jenkins/job/" + name + "/lastSuccessfulBuild/artifact/deploy/" + name + ".zip";

var root = moduleManager.getBean("root").getAbsolutePath();
var web = root + "/WEB-INF";
var tmp = web + "/tmp";

log.info("1. GET THE LATEST WAR FILE");
var downloader = new Downloader();
downloader.download( war, tmp + "/" + name + ".zip");

log.info("2. UNZIP WAR FILE");
var unziper = new ZipUtil();
unziper.unzip(  tmp + "/" + name + ".zip",  tmp );
log.info("2.a UNZIPPING TO:" + tmp + "/" );

log.info("3. REPLACE LIBS");
var files = new FileUtils();

files.deleteMatch( web + "/lib/" + name + "*.jar");
files.deleteMatch( web + "/lib/commons-dbcp-*.jar");
files.deleteMatch( web + "/lib/mysql-connector-java-*.jar");

files.copyFileByMatch( tmp + "/lib/*.jar", web + "/lib/");
//files.copyFileToDirectory(tmp + "/" + name + ".zip", "/media/services/extensions/");
files.copyFileByMatch(tmp + "/" + name + ".zip", "/media/services/extensions/");

log.info("4. CLEAN UP");
files.deleteAll(tmp);

log.info("5. UPGRADE COMPLETED");

