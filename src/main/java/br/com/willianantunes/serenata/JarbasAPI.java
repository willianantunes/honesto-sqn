package br.com.willianantunes.serenata;

import br.com.willianantunes.serenata.model.Pagination;
import br.com.willianantunes.serenata.model.Receipt;
import br.com.willianantunes.serenata.model.Reimbursement;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * @see <a href="https://github.com/okfn-brasil/serenata-de-amor/tree/master/jarbas#json-api-endpoints">Jarbas JSON API endpoints</a>
 */
@Path("/api")
public interface JarbasAPI {

    String API_DEFAULT_URL = "https://jarbas.serenata.ai";

    @GET
    @Path("/chamber_of_deputies/reimbursement")
    @Produces(MediaType.APPLICATION_JSON)
    Pagination findReimbursement(@QueryParam("search") String search);

    @GET
    @Path("/chamber_of_deputies/reimbursement")
    @Produces(MediaType.APPLICATION_JSON)
    Pagination findReimbursementWithYear(@QueryParam("search") String search, @QueryParam("year") Integer year);

    @GET
    @Path("/chamber_of_deputies/reimbursement/{documentId}")
    @Produces(MediaType.APPLICATION_JSON)
    Reimbursement reimbursementByDocumentId(@PathParam("documentId") Integer documentId);

    @GET
    @Path("/chamber_of_deputies/reimbursement/{documentId}/receipt")
    @Produces(MediaType.APPLICATION_JSON)
    Receipt reimbursementReceipt(@PathParam("documentId") Integer documentId);
}