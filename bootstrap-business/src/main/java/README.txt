How to create a new use case : "project"
 - Create a new package : "com.toyota.a2d.bo.project" in "src/main/java"
 - Create a new package : "com.toyota.a2d.bo.project" in "src/test/java"
 - Create a new class : "com.toyota.a2d.bo.project.ProjectResource" in "src/main/java"
 - Create a new class : "com.toyota.a2d.bo.project.ProjectResourceTest" in "src/test/java"
 - Add annotations :
 	@Path("/project")
	@Slf4j
	@Service
 - Apply this strategy :
 	- First look in the HTML file (main.html) for required data of the first display
 	- Then either update the main JavaScript file (main.js) either copy/paste the TEMPLATE one. If template is chosen, apply the template procedure. See "toyota-a2d-http/README.txt"
 	- Then look in the main.js for the relevant Ajax DataTable or Form to fill
 	- Create required JAX-RS services (one by one) : see "BrowserResource", "WineResource",... samples
 		- Add the right @POST , @GET,... (and should GET for the first service)
 		- Add the JSon producer @Produces(MediaType.APPLICATION_JSON) as needed
 		- Add the optional parameters
 			- Either the one of the HttRequest --> plain Java parameters
 			- Either an unique parameter "@Context final UriInfo uriInfo" for a DataTable query to manage the pagination
 			- Either some @Multipart(value = "partName") parameters if you have file as input, this parameter can have "required" annotation parameter
 		- Create/Reuse the right business bean (not Entity), "com.toyota.a2d.bo.project.Projet" for sample
 		- Name the bean's properties carefully : case, character error cause a JSon serialization error.
 	- Add in "rest-context.xml" file the reference 	"<ref bean="projectResource" />" inside "<jaxrs:serviceBeans>" tag.
 	- In case of DataTable data provider service, use the pagination utilities with the right DAO.
 	- Create either a DAO either a Repository:
 		- DAO : "com.toyota.a2d.dao.IProjectDao" + "com.toyota.a2d.dao.impl.ProjectDao" --> Full JQL query manipulation, requiring either native SQL either dynamic JQL (very rare)
 		- Repository :"com.toyota.a2d.dao.ProjectRepository" --> the other case (interface extends JpaRepository<ProjectEntity, ProjectEntityIdType> )
 	- Create the needed mapper (maybe two-ways) to convert entity to business bean or business bean to entity. Name them : TO_BUSINESS_CONVERTER and TO_JPA_CONVERTER
 	- In case of DataTable management, think to create the column mapping for ordering. Name it ORDERED_COLUMNS.
 - Launch the JUnit test ProjectResourceTest --> green = OK for commit
 	- Try to get a 100% code coverage
 	- Depending on the required features
 	- Configure your test to be the fastest by including the minimum spring configuration files :
 		- I need a data base connection : jpa-context-test.xml, also need to extend AbstractJpaTest
 		- I need security checks :        security-context.xml, also need to extend AbstractSecurityTest
 		- I need business layer :         business-context.xml, also need to extend AbstractSecurityTest
 		- I need REST layer :             rest-context.xml, also need to extends AbstractRestTest
 		- I need ESB features :           esb-context.xml
 		- I need JMS features :           jms-context-test.xml
 		- I need basic features :         core-context.xml
 		
