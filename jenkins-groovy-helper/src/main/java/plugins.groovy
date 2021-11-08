import jenkins.model.*

def plugins = jenkins.model.Jenkins.instance.getPluginManager().getPlugins()
//plugins.each {println "${it.getShortName()}: ${it.getVersion()}"}
plugins.each {}
plugins.each {println "${it.getShortName()}"}