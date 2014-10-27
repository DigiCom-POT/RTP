package sagar.hbase.stargate.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gson.Gson;

public class SimpleClient {
	public static void main(String[] args) throws ClientProtocolException,
			IOException {
		
		HttpClient client = new DefaultHttpClient();
				
		String userId = "3990";
		String movieId = "6659";
		// On Item Based recommendation
		List<String> getUserPreferences = getUserPreferences(userId, client);
		//System.out.println(getUserPreferences);
		
		List<String> movies = getRecommendedMovie(getUserPreferences, client);
		for(String mov : movies) {
			System.out.println(mov);
		}
		System.out.println("=============================================");
		List<String> m = new ArrayList<String>();
		m.add(movieId);
		List<String> movm = getRecommendedMovie(m, client);
		for(String mov : movm) {
			System.out.println("Movies Similar to : "  +mov);
		}
		
		
		List<String> movmovlist = getSimilarMovies(movieId, client);
		List<String> movmov = getRecommendedMovie(movmovlist, client);
		for(String mov : movmov) {
			System.out.println(mov);
		}
	}

	public static List<String> getSimilarMovies(String movieId,
			HttpClient client) throws ClientProtocolException, IOException {
		List<String> items = new ArrayList<String>();
			HttpGet request = new HttpGet("http://localhost:8500/movmovrec/"+movieId);
			request.setHeader(HttpHeaders.ACCEPT, "application/json");
			HttpResponse response = client.execute(request);
			BufferedReader rd = new BufferedReader(new InputStreamReader(response
					.getEntity().getContent()));
			String line = null;
			
			while ((line = rd.readLine()) != null) {
				//System.out.println(line);
				Gson json = new Gson();
				UserRecommendation rec = json.fromJson(line, UserRecommendation.class);
				//System.out.println(decode(rec.getRow()[0].getKey()));
				String[] as = decode(rec.getRow()[0].getCell()[0].get$()).split("\\s*\\),\\(\\s*");
				System.out.println("-----" + decode(rec.getRow()[0].getCell()[0].get$()));
				for(String s : as) {
					items.add(s.split(",")[1]);
				}
			}
		return items;
	}

	private static List<String> getRecommendedMovie(
			List<String> getUserPreferences, HttpClient client) throws ClientProtocolException, IOException {
		
		List<String> items = new ArrayList<String>();
		for(String movieID: getUserPreferences) {
			HttpGet request = new HttpGet("http://localhost:8500/moviesinfo/"+movieID);
			request.setHeader(HttpHeaders.ACCEPT, "application/json");
			HttpResponse response = client.execute(request);
			BufferedReader rd = new BufferedReader(new InputStreamReader(response
					.getEntity().getContent()));
			String line = null;
			
			while ((line = rd.readLine()) != null) {
				//System.out.println(line);
				Gson json = new Gson();
				UserRecommendation rec = json.fromJson(line, UserRecommendation.class);
				/*System.out.println(decode(rec.getRow()[0].getKey()));
				System.out.println("===" + decode(rec.getRow()[0].getCell()[0].get$()));
				System.out.println("===" + decode(rec.getRow()[0].getCell()[1].get$()));*/
				items.add(decode(rec.getRow()[0].getCell()[0].get$()) + "\t" +
						decode(rec.getRow()[0].getCell()[1].get$()));
			}
		}
		return items;
	}

	private static List<String> getUserPreferences(String userId, HttpClient client) throws ClientProtocolException, IOException {
		HttpGet request = new HttpGet("http://localhost:8500/moviesrecommend/"+userId);
		request.setHeader(HttpHeaders.ACCEPT, "application/json");
		HttpResponse response = client.execute(request);
		BufferedReader rd = new BufferedReader(new InputStreamReader(response
				.getEntity().getContent()));
		String line = null;
		List<String> items = null;
		while ((line = rd.readLine()) != null) {
			//System.out.println(line);
			Gson json = new Gson();
			UserRecommendation rec = json.fromJson(line, UserRecommendation.class);
			//System.out.println(decode(rec.getRow()[0].getKey()));
			//System.out.println(decode(rec.getRow()[0].getCell()[0].get$()));
			items = Arrays.asList(decode(rec.getRow()[0].getCell()[0].get$()).split("\\s*,\\s*"));
		}
		List<String> filter = new ArrayList<String>();
		for(String s : items) {
			s = s.replace("[", "");
			s = s.replace("]", "");
			String[] n = s.split(":");
			filter.add(n[0]);
		}
		
		return filter;
	}
	
	public static String decode(String s) {
	    return StringUtils.newStringUtf8(Base64.decodeBase64(s));
	}
	public static String encode(String s) {
	    return Base64.encodeBase64String(StringUtils.getBytesUtf8(s));
	}
}
