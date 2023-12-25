package com.RoboRallyServer.business.abstracts;

import com.RoboRallyServer.entities.DefCompetitors;
import com.RoboRallyServer.utilities.results.DataResult;
import com.RoboRallyServer.utilities.results.Result;

import java.util.List;

public interface DefCompetitorsService {

    Result add(DefCompetitors competitors);

    DataResult<List<DefCompetitors>> getAllCompetitorsByDuration();

    Result delete(int id);
    Result update(DefCompetitors competitors);

    DataResult<DefCompetitors> getById(int id);
    Result updateDurationById(int id,String duration);
    Result updateStartById(int id,Boolean start);

}
