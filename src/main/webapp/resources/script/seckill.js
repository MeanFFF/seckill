//存放主要交互逻辑js代码
//javascript 模块化
//模块名,模块下的具体职责
//seckill.detail.init(params)
var seckill ={
    //封装秒杀相关ajax的url
    URL : {
        now : function () {
            return '/seckill/time/now';
        },
        exposer : function (seckillId) {
            return '/seckill/'+seckillId+'/exposer';
        },
        execution : function (seckillId, md5) {
            return '/seckill/'+seckillId + '/'+md5 + '/execution';
        }
    },
    //处理秒杀逻辑
    //获取秒杀地址,控制显示逻辑,执行秒杀
    handleSeckill : function(seckillId, node){
        node.hide().html('<button class="btn btn-primary btn-lg" id="killBtn">Start</button>');
        $.post(seckill.URL.exposer(seckillId), {}, function (result) {
            //在回调函数中执行交互流程
            if(result && result['success']){
                var exposer = result['data'];
                if(exposer['exposed']){
                    //开启秒杀
                    //获取秒杀地址
                    var md5 = exposer['md5'];
                    var killUrl = seckill.URL.execution(seckillId, md5);
                    console.log('killUrl:' + killUrl);
                    //绑定一次点击事件
                    $('#killBtn').one('click', function () {
                        //绑定执行秒杀的操作
                        //1.先禁用按钮
                        $(this).addClass('disabled');
                        //2.发送请求执行秒杀
                        $.post(killUrl,{}, function (result) {
                            if(result && result['success']){
                                var killResult = result['data'];
                                var state = killResult['state'];
                                var stateInfo = killResult['stateInfo'];
                                //显示秒杀结果
                                node.html('<span class="label label-success">'+stateInfo +'</span>');
                            }
                        });
                    });
                    node.show();
                }else{
                    //未开启秒杀
                    var now = exposer['now'];
                    var start = exposer['start'];
                    var end = exposer['end'];
                    //重新计算计时逻辑
                    seckill.countdown(seckillId, now, start, end);
                }
            }else{
                console.log('result:' + result);
            }
        })
    },
    //验证手机号
    validdatePhone : function(phone){
        if(phone && phone.length == 11 && !isNaN(phone)){
            return true;
        }else{
            return false;
        }
    },
    countdown:function(seckillId, nowTime, startTime, endTime){
        var seckillBox = $('#seckill-box');
        //时间的判断
        if(nowTime > endTime){
            //秒杀结束
            seckillBox.html('seckill showdown');
        }else if(nowTime < startTime){
            //秒杀未开始,计时事件绑定
            var killTime = new Date(startTime + 1000);

            seckillBox.countdown(killTime, function (event) {
                //时间格式
                var format = event.strftime('countdown: %Ddays %Hhours %Mminutes %Sseckonds');
                seckillBox.html(format);
                /**
                 * 时间完成后回调事件
                 */
            }).on('finish.countdown', function () {
                //获取秒杀地址,控制显示逻辑,执行秒杀
                seckill.handleSeckill(seckillId, seckillBox);
            });
        }else{
            //秒杀开始
            seckill.handleSeckill(seckillId, seckillBox);
        }
    },
    //详情页秒杀逻辑
    detail : {
        //详情页初始化
        init : function (params) {
            //用户手机验证和登录,计时交互
            //规划我们的交互流程
            //在cookie中查找手机号
            var killPhone = $.cookie('killPhone');

            //验证手机号
            if(!seckill.validdatePhone(killPhone)){
                //绑定phone
                //控制输出
                var killPhoneModal = $('#killPhoneModal');
                killPhoneModal.modal({

                    show : true,//显示弹出层
                    backdrop:'static',//禁止位置关闭
                    keyboard:false//关闭键盘事件
                });

                $('#killPhoneBtn').click(function () {
                    var inputPhone = $('#killPhoneKey').val();
                    if(seckill.validdatePhone(inputPhone)){
                        //电话写入cookie
                        $.cookie('killPhone', inputPhone, {expires:7, path:'/seckill'});
                        //刷新页面
                        window.location.reload();
                    }else{
                        $('#killPhoneMessage').hide().html('<label class="label label-danger">Error</label>').show(300);
                    }
                });
            }
            var startTime = params['startTime'];
            var endTime = params['endTime'];
            var seckillId = params['seckillId'];
            $.get(seckill.URL.now(), {}, function (result) {
                if(result && result['success']){
                    var nowTime = result['data'];
                    //事件判断
                    seckill.countdown(seckillId,nowTime,startTime,endTime);
                }else{
                    console.log('result'+result);
                }
            });
        }
    }
}