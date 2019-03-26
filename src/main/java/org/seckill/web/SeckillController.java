package org.seckill.web;
import java.util.Date;
import java.util.List;

import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.dto.SeckillResult;
import org.seckill.entity.Seckill;
import org.seckill.enums.SeckillStateEnum;
import org.seckill.execption.RepeatKillException;
import org.seckill.execption.SeckillCloseException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping("/seckill")//url:/模块/资源/{id}/细分
public class SeckillController {
    private final Logger logger = LoggerFactory.getLogger(SeckillController.class);
    @Autowired
    private SeckillService seckillService;

    @RequestMapping(value="/list",method=RequestMethod.GET)
    public String list(Model model){
        //获取列表
        List<Seckill> list = seckillService.getSeckillList();
        model.addAttribute("list", list);
        return "list";
    }

    @RequestMapping(value="/{seckillId}/detail",method=RequestMethod.GET)
    public String detail(@PathVariable("seckillId") Long seckillId,Model model){
        if(seckillId==null){
            return "redirect:/seckill/list";
        }
        Seckill seckill = seckillService.getById(seckillId);
        if (seckill == null) {
            return "forword:/seckill/list";
        }
        model.addAttribute("seckill", seckill);
        return "detail";
    }
    //ajax json
    @ResponseBody
    @RequestMapping(value="/{seckillId}/exposer",
            method=RequestMethod.POST,
            produces={"application/json;charset=UTF-8"})
    public SeckillResult<Exposer> exposer(@PathVariable("seckillId")Long seckillId){
        SeckillResult<Exposer> result;
        try{
            Exposer exposer = seckillService.exportSeckillUrl(seckillId);
            result = new SeckillResult<Exposer>(true, exposer);
        }catch(Exception e){
            logger.error(e.getMessage(),e);
            result = new SeckillResult<Exposer>(false, e.getMessage());
        }
        return result;
    }
    @ResponseBody
    @RequestMapping(value="/{seckillId}/{md5}/execution",
            method=RequestMethod.POST,
            produces={"application/json;charset=UTF-8"})
    public SeckillResult<SeckillExecution> execute(@PathVariable("seckillId")Long seckillId,
                                                   @PathVariable("md5")String md5,
                                                   @CookieValue(value="killPhone",required=false)Long phone){
        //SeckillResult<SeckillExecution> result;

        //也可以使用springmvc valid
        if (phone==null) {
            return new SeckillResult<>(false, "未注册");
        }
        try{
            SeckillExecution execution = seckillService.executeSeckill(seckillId, phone, md5);

            return new SeckillResult<SeckillExecution>(true, execution);
        }catch(RepeatKillException e){
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStateEnum.REPEAT_KILL);
            return new SeckillResult<SeckillExecution>(true, execution);
        }catch(SeckillCloseException e){
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStateEnum.END);
            return new SeckillResult<SeckillExecution>(true, execution);
        }catch(Exception e){
            logger.error(e.getMessage(),e);
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStateEnum.INNER_ERROR);
            return new SeckillResult<SeckillExecution>(true, execution);
        }

    }
    @ResponseBody
    @RequestMapping(value="/time/now",method=RequestMethod.GET)
    public SeckillResult<Long> time (){
        Date now = new Date();
        return new SeckillResult<Long>(true, now.getTime());
    }
}