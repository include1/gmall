package com.zm.gmall.search;

import com.alibaba.dubbo.config.annotation.Reference;
import com.zm.gmall.bean.PmsSearchSkuInfo;
import com.zm.gmall.bean.PmsSkuInfo;
import com.zm.gmall.service.SkuService;
import io.searchbox.client.JestClient;

import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallSearchServiceApplicationTests {
    @Reference
    SkuService skuService;
    @Autowired
    JestClient jestClient;
    @Test
    public  void contextLoads() throws IOException {
        //使用jest的dsl工具
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            //bool:关联查询
            BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
            //filter：过滤查询
//            String[] str = {"39","40","41"};
//            TermsQueryBuilder termsQueryBuilder = new TermsQueryBuilder("skuAttrValueList.valueId",str);
//            boolQueryBuilder.filter(termsQueryBuilder);
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId","39");
            boolQueryBuilder.filter(termQueryBuilder);
//            TermQueryBuilder termQueryBuilder1 = new TermQueryBuilder("skuAttrValueList.valueId","40");
//            boolQueryBuilder.filter(termQueryBuilder1);
            //must：关键词查询
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName","华为");
            boolQueryBuilder.must(matchQueryBuilder);
        //query:查询
        searchSourceBuilder.query(boolQueryBuilder);
        //from
        searchSourceBuilder.from(0);
        //size
        searchSourceBuilder.size(10);
        //highlight
        searchSourceBuilder.highlight(null);

        String DslStr = searchSourceBuilder.toString();
        System.out.println(DslStr);
        List<PmsSearchSkuInfo> pmsSearchSkuInfoList = new ArrayList<>();
        //用api执行复杂语句,获取elasticsearch中的数据
        Search build = new Search.Builder(DslStr).addIndex("gmall0105").addType("PmsSkuInfo").build();
        //包装dls语句
        SearchResult execute = jestClient.execute(build);
        List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = execute.getHits(PmsSearchSkuInfo.class);
        for(SearchResult.Hit<PmsSearchSkuInfo, Void> hit: hits){
            PmsSearchSkuInfo source = hit.source;
            pmsSearchSkuInfoList.add(source);
        }
        System.out.println(pmsSearchSkuInfoList.size());
        System.out.println();
    }

    @Test
    public void put() throws IOException {
        //向elasticsearch添加数据
        //查询mysql数据
        List<PmsSkuInfo> pmsSkuInfoList = new ArrayList<>();
        pmsSkuInfoList = skuService.getAllSkuInfo();
        List<PmsSearchSkuInfo> pmsSearchSkuInfoList = new ArrayList<>();
        //把pmsSkuInfo的属性值复制到pmssearchskuInfo
        for(PmsSkuInfo pmsSkuInfo:pmsSkuInfoList){
            PmsSearchSkuInfo pmsSearchSkuInfo = new PmsSearchSkuInfo();
            BeanUtils.copyProperties(pmsSkuInfo,pmsSearchSkuInfo);
            pmsSearchSkuInfoList.add(pmsSearchSkuInfo);
        }
        for(PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfoList) {
            // 转化为es的数据结构
            Index build = new Index.Builder(pmsSearchSkuInfo).index("gmall0105").type("PmsSkuInfo").id(pmsSearchSkuInfo.getId()).build();
            //导入es/这是http请求，与jdbc连接数据库不同
            jestClient.execute(build);
        }

    }
}
