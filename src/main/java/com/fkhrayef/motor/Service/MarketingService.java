package com.fkhrayef.motor.Service;

import com.fkhrayef.motor.Api.ApiException;
import com.fkhrayef.motor.DTOin.MarketingDTO;
import com.fkhrayef.motor.Model.Marketing;
import com.fkhrayef.motor.Repository.MarketingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MarketingService {

    private final MarketingRepository marketingRepository;

    public List<Marketing> getAllMarketing(){
        return marketingRepository.findAll();
    }

    public void addMarketing(MarketingDTO marketingDTO){
        Marketing marketing = new Marketing();

        marketing.setTitle(marketingDTO.getTitle());
        marketing.setOfferType(marketingDTO.getOfferType());
        marketing.setDescription(marketingDTO.getDescription());
        marketing.setPosterUrl(marketingDTO.getPosterUrl());
        marketing.setStartDate(marketingDTO.getStartDate());
        marketing.setEndDate(marketingDTO.getEndDate());

        marketingRepository.save(marketing);
    }

    public void updateMarketing(Integer id , MarketingDTO marketingDTO){
        Marketing marketing = marketingRepository.findMarketingById(id);
        if (marketing == null){
            throw new ApiException("Marketing not found");
        }

        marketing.setTitle(marketingDTO.getTitle());
        marketing.setOfferType(marketingDTO.getOfferType());
        marketing.setDescription(marketingDTO.getDescription());
        marketing.setPosterUrl(marketingDTO.getPosterUrl());
        marketing.setStartDate(marketingDTO.getStartDate());
        marketing.setEndDate(marketingDTO.getEndDate());

        marketingRepository.save(marketing);
    }

    public void deleteMarketing (Integer id){
        Marketing marketing = marketingRepository.findMarketingById(id);
        if (marketing == null){
            throw new ApiException("Marketing not found");
        }
        marketingRepository.delete(marketing);
    }
}
