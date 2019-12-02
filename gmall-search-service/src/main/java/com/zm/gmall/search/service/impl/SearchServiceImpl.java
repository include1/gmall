package com.zm.gmall.search.service.impl;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Service;
import com.sun.org.apache.xpath.internal.operations.Bool;
import com.zm.gmall.bean.PmsBrand;
import com.zm.gmall.bean.PmsSearchParam;
import com.zm.gmall.bean.PmsSearchSkuInfo;
import com.zm.gmall.bean.PmsSkuAttrValue;
import com.zm.gmall.search.dao.PmsBrandMapper;
import com.zm.gmall.service.SearchServcie;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.query.SearchQuery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//该服务注册到dubbo中
@Service
public class SearchServiceImpl implements SearchServcie {
    @Autowired
    JestClient jestClient;
    @Autowired
    PmsBrandMapper pmsBrandMapper;

    private String getSearchDsl(PmsSearchParam pmsSearchParam){
        String[] pmsSkuAttrValueList = pmsSearchParam.getValueId();
        String keyword = pmsSearchParam.getKeyword();
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        //使用jest的工具类编写del语句查询elasticsearch
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //bool
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //filter：过滤
        if(!StringUtils.isBlank(catalog3Id)){
            TermsQueryBuilder termsQueryBuilder = new TermsQueryBuilder("catalog3Id",pmsSearchParam.getCatalog3Id());
            boolQueryBuilder.filter(termsQueryBuilder);
        }
        if(pmsSkuAttrValueList != null) {
            for(String valueId : pmsSkuAttrValueList) {
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", valueId);
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }
        //mute:关键字
        if(!StringUtils.isBlank(keyword)) {
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", keyword);
            boolQueryBuilder.must(matchQueryBuilder);
        }
        //query
        searchSourceBuilder.query(boolQueryBuilder);
        //from
        searchSourceBuilder.from(0);
        //size
        searchSourceBuilder.size(20);
        //highlight
        //设置高亮显示
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.field("skuName");
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlight(highlightBuilder);
        //设置排序
        SearchSourceBuilder sort = searchSourceBuilder.sort("price", SortOrder.DESC);//降序排列

        return  searchSourceBuilder.toString();
    }
    @Override
    public List<PmsSearchSkuInfo> list(PmsSearchParam pmsSearchParam) {
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = new ArrayList<>();
        //生成Dls语句
        String dlsStr = getSearchDsl(pmsSearchParam);
        //查询elastic中的数据
        Search build = new Search.Builder(dlsStr).addIndex("gmall0105").addType("PmsSkuInfo").build();
        //包装dls语句
        SearchResult execute = null;
        try {
            execute = jestClient.execute(build);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //查询出来的结果
        List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = execute.getHits(PmsSearchSkuInfo.class);
        for(SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits){
            //获取资源
            PmsSearchSkuInfo source = hit.source;

            //获取highlight标签的内容
            Map<String, List<String>> highlight = hit.highlight;
            if(highlight != null) {
                //替换原来是skuName的属性
                String skuName = highlight.get("skuName").get(0);
                source.setSkuName(skuName);
            }
            pmsSearchSkuInfos.add(source);
        }
        return pmsSearchSkuInfos;
    }

    @Override
    public List<PmsBrand> getPmsBrandList() {
        return pmsBrandMapper.selectAll();
    }
}
