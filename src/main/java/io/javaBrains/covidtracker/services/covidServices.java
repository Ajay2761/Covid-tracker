package io.javaBrains.covidtracker.services;

import io.javaBrains.covidtracker.services.models.LocationStats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class covidServices {
    private static String virus_data_url="https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/who_covid_19_situation_reports/who_covid_19_sit_rep_time_series/who_covid_19_sit_rep_time_series.csv";
  private List<LocationStats>allStats=new ArrayList<>();

    public List<LocationStats> getAllStats() {
        return allStats;
    }

   @PostConstruct
   @Scheduled (cron = "* * * 1 * *")
    public void fetchData() throws IOException, InterruptedException {
       List<LocationStats>newStats=new ArrayList<>();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                   .uri(URI.create(virus_data_url))
                    .build();
        HttpResponse<String>httpResponse =   client.send(request,HttpResponse.BodyHandlers.ofString());
        StringReader csvBodyReader = new StringReader(httpResponse.body());
System.out.print(httpResponse);

        Iterable<CSVRecord>records= CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);
        for(CSVRecord record:records){
            LocationStats locationStat=new LocationStats();
            locationStat.setState(record.get("Province/States"));
            locationStat.setCountry(record.get("Country/Region"));
            if(StringUtils.hasText(record.get(record.size()-60))){
                locationStat.setLatestTotalCases(Integer.parseInt(record.get(record.size()-60)));
                int latestCases = Integer.parseInt(record.get(record.size() - 60));
                int prevDayCases = Integer.parseInt(record.get(record.size() - 59));
                locationStat.setLatestTotalCases(latestCases);
                locationStat.setDiffFromPrevDay(latestCases - prevDayCases);

            }
            else{
                locationStat.setLatestTotalCases(0);
            }
            newStats.add(locationStat);
        }
        this.allStats=newStats;
    }

}