import hudson.model.*
import jenkins.model.*
import hudson.tools.*
import groovy.json.JsonSlurper

env = System.getenv()
sonar_server_url = env['SONAR_SERVER_URL']
sonar_user = env['JENKINS_USER']
sonar_pass = env['JENKINS_PASS']
token_name = "jenkins"

def isTokenExists = searchExistingToken()
if (isTokenExists){
    def createToken = new URL(sonar_server_url + "/api/user_tokens/generate?name=$token_name").openConnection() as HttpURLConnection;
    createToken.setRequestMethod("POST")
    setRequestProperties(createToken)
    if (createToken.getResponseCode() == 200){
        println "Sonar Auth Token generated successfully!"
        def result = createToken.inputStream.withCloseable { inStream ->
            new JsonSlurper().parse(inStream as InputStream)
        }
        def token = result.token
        println "Token name: $token_name Value: $token"
            System.setProperty("SONAR_AUTH_TOKEN",token)
    }
}

private boolean searchExistingToken() {
    def searchTokens = new URL(sonar_server_url + "/api/user_tokens/search").openConnection() as HttpURLConnection;
    searchTokens.setRequestMethod("POST")
    setRequestProperties(searchTokens)

    if (searchTokens.getResponseCode() == 200) {
        def result = searchTokens.inputStream.withCloseable { inStream ->
            new JsonSlurper().parse(inStream as InputStream)
        }
        def userTokens = result.userTokens
        for (def token : userTokens) {
            if (token.name == token_name) {
                println "Sonar Auth Token already exists for Jenkins..."
                return true
            }
        }
        return false
    }
}

private void setRequestProperties(HttpURLConnection connection) {
    connection.setDoOutput(true)
    connection.setRequestProperty("Content-Type", "application/json")
    connection.setRequestProperty('Accept', 'application/json')
    def basicAuth = "Basic " + "$sonar_user:$sonar_pass".bytes.encodeBase64()
    connection.setRequestProperty("Authorization", basicAuth)
}
