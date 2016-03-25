/**
 *    Copyright 2015-2016 Austin Keener & Michael Ritter
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
package net.dv8tion.jda.requests;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.BaseRequest;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.body.RequestBodyEntity;
import net.dv8tion.jda.JDAInfo;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.utils.SimpleLog;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Requester
{
    public static final SimpleLog LOG = SimpleLog.getLog("JDARequester");
    public static final String USER_AGENT = "JDA DiscordBot (" + JDAInfo.GITHUB + ", " + JDAInfo.VERSION + ")";
    public static final String DISCORD_API_PREFIX = "https://discordapp.com/api/";

    private final JDAImpl api;

    public Requester(JDAImpl api)
    {
        this.api = api;
    }

    public JSONObject get(String url)
    {
        return toObject(addHeaders(Unirest.get(url)));
    }

    public JSONObject delete(String url)
    {
        return toObject(addHeaders(Unirest.delete(url)));
    }

    public JSONObject post(String url, JSONObject body)
    {
        return toObject(addHeaders(Unirest.post(url)).body(body.toString()));
    }

    public JSONObject patch(String url, JSONObject body)
    {
        return toObject(addHeaders(Unirest.patch(url)).body(body.toString()));
    }

    public JSONObject put(String url, JSONObject body)
    {
        return toObject(addHeaders(Unirest.put(url)).body(body.toString()));
    }

    public JSONArray getA(String url)
    {
        return toArray(addHeaders(Unirest.get(url)));
    }

    public JSONArray deleteA(String url)
    {
        return toArray(addHeaders(Unirest.delete(url)));
    }

    public JSONArray postA(String url, JSONObject body)
    {
        return toArray(addHeaders(Unirest.post(url)).body(body.toString()));
    }

    public JSONArray patchA(String url, JSONObject body)
    {
        return toArray(addHeaders(Unirest.patch(url)).body(body.toString()));
    }

    public JSONArray patchA(String url, JSONArray body)
    {
        return toArray(addHeaders(Unirest.patch(url)).body(body.toString()));
    }

    private JSONObject toObject(BaseRequest request)
    {
        String body = null;
        try
        {
            String dbg = String.format("Requesting %s -> %s\n\tPayload: %s\n\tResponse: ", request.getHttpRequest().getHttpMethod().name(),
                    request.getHttpRequest().getUrl(), ((request instanceof RequestBodyEntity) ? ((RequestBodyEntity) request).getBody().toString() : "None"));
            body = request.asString().getBody();
            if (body != null && body.startsWith("<"))
            {
                LOG.debug(String.format("Requesting %s -> %s returned HTML... retrying", request.getHttpRequest().getHttpMethod().name(), request.getHttpRequest().getUrl()));
                try
                {
                    Thread.sleep(50);
                }
                catch (InterruptedException ignored) {}
                body = request.asString().getBody();
            }
            LOG.trace(dbg + body);
            return body == null ? null : new JSONObject(body);
        }
        catch (UnirestException e)
        {
            if (LOG.getEffectiveLevel().compareTo(SimpleLog.Level.DEBUG) != 1)
            {
                LOG.log(e);
            }
        }
        catch (JSONException e)
        {
            LOG.fatal("Following json caused an exception: " + body);
            LOG.log(e);
        }
        return null;
    }

    private JSONArray toArray(BaseRequest request)
    {
        String body = null;
        try
        {
            String dbg = String.format("Requesting %s -> %s\n\tPayload: %s\n\tResponse: ", request.getHttpRequest().getHttpMethod().name(),
                    request.getHttpRequest().getUrl(), ((request instanceof RequestBodyEntity)? ((RequestBodyEntity) request).getBody().toString():"None"));
            body = request.asString().getBody();
            if (body != null && body.startsWith("<"))
            {
                LOG.debug(String.format("Requesting %s -> %s returned HTML... retrying", request.getHttpRequest().getHttpMethod().name(), request.getHttpRequest().getUrl()));
                try
                {
                    Thread.sleep(50);
                }
                catch (InterruptedException ignored) {}
                body = request.asString().getBody();
            }
            LOG.trace(dbg + body);
            return body == null ? null : new JSONArray(body);
        }
        catch (UnirestException e)
        {
            if (LOG.getEffectiveLevel().compareTo(SimpleLog.Level.DEBUG) != 1)
            {
                LOG.log(e);
            }
        }
        catch (JSONException e)
        {
            LOG.fatal("Following json caused an exception: " + body);
            LOG.log(e);
        }
        return null;
    }

    private <T extends HttpRequest> T addHeaders(T request)
    {
        //adding token to all requests to the discord api or cdn pages
        //can't check for startsWith(DISCORD_API_PREFIX) due to cdn endpoints
        if (api.getAuthToken() != null && request.getUrl().contains("discordapp.com"))
        {
            request.header("authorization", api.getAuthToken());
        }
        if (!(request instanceof GetRequest))
        {
            request.header("Content-Type", "application/json");
        }
        request.header("user-agent", USER_AGENT);
        request.header("Accept-Encoding", "gzip");
        return request;
    }
}
