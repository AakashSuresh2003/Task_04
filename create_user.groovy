import jenkins.model.*
import hudson.security.*
// Set Jenkins instance
def instance = Jenkins.getInstance()
// 1. Create first-time user
def hudsonRealm = new HudsonPrivateSecurityRealm(false)
hudsonRealm.createAccount('new_admin_user', 'new_secure_password')  // Replace with desired username and password
instance.setSecurityRealm(hudsonRealm)
// Set admin permissions for the user
def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
strategy.setAllowAnonymousRead(false)  // Disable anonymous access
instance.setAuthorizationStrategy(strategy)
println("User 'new_admin_user' created successfully!")
// 2. Install suggested plugins
def pluginManager = instance.getPluginManager()
def updateCenter = instance.getUpdateCenter()
// List of suggested plugins (can be customized)
def plugins = [
    'workflow-aggregator', // Pipeline plugin
    'git',                 // Git plugin
    'blueocean',           // Blue Ocean UI
    'sonar',               // SonarQube plugin
    'pipeline-utility-steps',
    'email-ext'            // Email extension plugin
]
plugins.each { pluginName ->
    if (!pluginManager.getPlugin(pluginName)) {
        println("Installing plugin: ${pluginName}")
        def plugin = updateCenter.getPlugin(pluginName)
        if (plugin) {
            plugin.deploy()
        }
    } else {
        println("Plugin ${pluginName} is already installed.")
    }
}
// Save Jenkins instance to apply changes
instance.save()
println("Jenkins setup complete with new user and plugins.")