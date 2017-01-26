/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package com.shelley.diary.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.googlecode.objectify.cmd.Query;

import static com.shelley.diary.backend.OfyService.ofy;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

/**
 * An endpoint class we are exposing
 */
@Api(
        name = "myApi",
        version = "1",
        namespace = @ApiNamespace(
                ownerDomain = "backend.diary.shelley.com",
                ownerName = "backend.diary.shelley.com",
                packagePath = ""
        )
)

public class MyEndpoint {

    /**
     * Return a collection of diaries.
     *
     * @param userid List diaries with this filter.
     * @return A list of Diaries.
     */
    @ApiMethod(name = "listDiaries")
    public CollectionResponse<MyDiary> listDiaries(@Named("userid") String userid) {

        Query<MyDiary> query = ofy().load().type(MyDiary.class).filter("user_id = ", userid);

        List<MyDiary> records = new ArrayList<>();

        QueryResultIterator<MyDiary> iterator = query.iterator();
        while (iterator.hasNext()) {
            records.add(iterator.next());
        }

        return CollectionResponse.<MyDiary>builder().setItems(records).build();
    }

    /**
     * This inserts a new <code>Diaries/code> object.
     * @param diary The object to be added.
     * @return The object to be added.
     */
    @ApiMethod(name = "insertDiary")
    public MyDiary insertDiary(MyDiary diary) throws ConflictException {

        //If if is not null, then check if it exists. If yes, throw an Exception
        //that it is already present
        if (diary.getDiaryId() != null) {
            if (findRecord(diary.getDiaryId()) != null) {
                throw new ConflictException("Diary already exists");
            }
        }

        //Since our @Id field is a Long, Objectify will generate a unique value for us
        //when we use put
        ofy().save().entity(diary).now();
        return diary;
    }

    /**
     * This updates an existing <code>Diary</code> object.
     * @param diary The object to be added.
     * @return The object to be updated.
     */
    @ApiMethod(name = "updateDiary")
    public MyDiary updateDiary(MyDiary diary)throws NotFoundException {

        if (findRecord(diary.getDiaryId()) == null) {
            throw new NotFoundException("Diary Record does not exist");
        }

        ofy().save().entity(diary).now();
        return diary;
    }

    /**
     * This deletes an existing <code>Diary</code> object.
     * @param id The id of the object to be deleted.
     */
    @ApiMethod(name = "removeDiary")
    public void removeDiary(@Named("id") Long id) throws NotFoundException {
        MyDiary record = findRecord(id);
        if(record == null) {
            throw new NotFoundException("Diary Record does not exist");
        }
        ofy().delete().entity(record).now();
    }

    //Private method to retrieve a Diary record
    private MyDiary findRecord(Long id) {
        return ofy().load().type(MyDiary.class).id(id).now();
    }

    /**
     * This checks the database for existing <code>User</code> object.
     * @param id The id of the object to be verified.
     * @return An User object or Null is no User is found.
     */

    @ApiMethod(name = "verifyUser")
    public Users verifyUser(@Named("id") String id) {
        Users user = findUser(id);
        if(user == null) {
            return null;
        }
        return user;
    }

    /**
     * This inserts a new <code>Users/code> object.
     * @param user The object to be added.
     * @return The object to be added.
     */
    @ApiMethod(name = "insertUser")
    public Users insertUser(Users user) throws ConflictException {

        if (user.getUserId() != null) {
            if (findUser(user.getUserId()) != null) {
                throw new ConflictException("User already exists");
            }
        }

        ofy().save().entity(user).now();
        return user;
    }

    //Private method to retrieve a User record
    private Users findUser(String id) {
        return ofy().load().type(Users.class).id(id).now();
    }
}
