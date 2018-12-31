import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Main {

	public static String getURLSource(String url) throws IOException {
		// gets a url and opens connection
		URL urlObject = new URL(url);
		URLConnection urlConnection = urlObject.openConnection();
		urlConnection.setRequestProperty("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");

		return toString(urlConnection.getInputStream());
	}

	private static String toString(InputStream inputStream) throws IOException {
		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
			String inputLine;
			StringBuilder stringBuilder = new StringBuilder();
			while ((inputLine = bufferedReader.readLine()) != null) {
				stringBuilder.append(inputLine);
			}

			return stringBuilder.toString();
		}
	}

	public static void main(String[] args) {
		JSONParser parser = new JSONParser();
		String s = "";
		try {
			// reads the Json into s
			s = getURLSource("https://api.exchangeratesapi.io/history?start_at=2018-03-01&end_at=2018-09-01&symbols=ILS,USD,GBP&base=USD");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			// converts from string to Json object because the api works with json object
			JSONObject obj = (JSONObject)parser.parse(s);
			
			obj = (JSONObject) parser.parse(obj.get("rates").toString());
			Set<String> keys = obj.keySet();
			// converts the keySet to arraylist for sorting the rate dates
			ArrayList<String> keysSorted = new ArrayList(keys);
			DateFormat format = new SimpleDateFormat("yyyy-M-d", Locale.ENGLISH);
			Date date = new Date();
			Date date2 = new Date();
			try {
				for(int i = 0; i < keysSorted.size() - 1; i++)
				{
					date = format.parse(keysSorted.get(i));
					for(int j = i+1; j < keysSorted.size(); j++)
					{
						date2 = format.parse(keysSorted.get(j));
						if(date2.before(date))
						{
							date = date2;
							String a = keysSorted.remove(j);
							String b = keysSorted.remove(i);
							keysSorted.add(i, a);
							keysSorted.add(j, b);
						}
					}
				}
				
				System.out.println(keysSorted);
				
				float prev = 0.0f;
				float moneyUSD = 0.0f; // USD
				float moneyILS = 348.0f;
				System.out.println("the start -> "+moneyILS+" NIS");
				float startMoney = moneyILS;
				boolean isFirst = true;
				float currentCurrency = 0;
				for(int i = 0; i < keysSorted.size(); i++)
				{
					JSONObject obj2 = (JSONObject)parser.parse(obj.get(keysSorted.get(i)).toString());
					
					currentCurrency = Float.parseFloat(obj2.get("ILS").toString());
					
					if(isFirst == false)
					{
						if((prev > currentCurrency) && (moneyILS==0)&&(moneyUSD!=0))
						{ //down -> Buy ILS on my current USD
							//if(moneyUSD == 0.0f)
								//continue;
							moneyILS = moneyUSD * currentCurrency;
							moneyUSD = 0.0f;
							System.out.println("BUY and get ILS: " + moneyILS);
						}
						else
						{ //up -> sell my current ILS and get USD
							if(moneyUSD==0&&moneyILS!=0)
							
							moneyUSD = moneyILS/currentCurrency;
							moneyILS = 0.0f;
							System.out.println("SELL and get USD: " + moneyUSD);
						}
					}
					else
					{
						isFirst = false;
					}
					
					prev = currentCurrency;
				}
				
				float end_sum = 0;
				if(moneyILS==0) {
					end_sum=moneyUSD*currentCurrency;
					System.out.println("the end -> "+end_sum+" NIS");
				}
				
				if(moneyUSD==0) {
					end_sum=moneyILS;
					System.out.println("the end -> "+end_sum+" NIS");
				}
				float percentage=((end_sum-startMoney)/startMoney)*100;
				System.out.println("We made a profit of -> "+percentage+"% from our start sum");
				if(percentage<0)
					System.out.println("We lost -> "+percentage+"% from our start sum");
				
			} catch (java.text.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
			System.out.println(date);
			
			/*
			// Parse the source code 
			JSONObject obj = (JSONObject) parser.parse(s);
			//JSONArray array = (JSONArray) obj;

			System.out.println("The 2nd element of array");
			//System.out.println(array.get(0));
			System.out.println(obj.get("date"));
			System.out.println();
			
			obj = (JSONObject) parser.parse(obj.get("rates").toString());
			
			 */
		} catch (ParseException pe) {

			System.out.println("position: " + pe.getPosition());
			System.out.println(pe);
		}
	}

}
