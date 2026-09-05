import hudson.security.FullControlOnceLoggedInAuthorizationStrategy
import hudson.security.HudsonPrivateSecurityRealm
import jenkins.model.Jenkins

def instance = Jenkins.get()
def realm = instance.securityRealm
if (!(realm instanceof HudsonPrivateSecurityRealm)) {
  realm = new HudsonPrivateSecurityRealm(false)
  instance.setSecurityRealm(realm)
}

def users = realm.getAllUsers()
if (users == null || users.isEmpty()) {
  def id = System.getenv("JENKINS_ADMIN_ID") ?: "admin"
  def password = System.getenv("JENKINS_ADMIN_PASSWORD") ?: "change_me_jenkins"
  realm.createAccount(id, password)
  def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
  strategy.setAllowAnonymousRead(false)
  instance.setAuthorizationStrategy(strategy)
  instance.save()
  println "Created local Jenkins admin '${id}'. Change JENKINS_ADMIN_PASSWORD for anything other than a machine de démo."
}
