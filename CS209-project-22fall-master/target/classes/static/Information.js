var obj;
var scatter_data;
var average;
var max;
var min;
var developers;
function echarts() {
    document.write("<script language=javascript src=" + "echarts.min.js" + "></script>");
}


function releaseNum(){
    $.ajax({
        url:'/repo/releaseNum',	//这是后端接口的url
        method:'get',
        success:function (res) {
            obj = res;
            document.getElementById('release_num').text(obj+" releases in total");
            //res便是的数据便是后端拿到的数据
            //这里需要注意：res为局部变量，
            //所以需要在方法外定义一个变量把res赋值给他，才能在方法之外使用。
        },
    })
}

function max(){
    $.ajax({
        url:'/repo/issue/solveTime/max',	//这是后端接口的url
        method:'get',
        success:function (res) {
            max = res;
            //res便是的数据便是后端拿到的数据
            //这里需要注意：res为局部变量，
            //所以需要在方法外定义一个变量把res赋值给他，才能在方法之外使用。
        },
    })
}

function min(){
    $.ajax({
        url:'/repo/issue/solveTime/min',	//这是后端接口的url
        method:'get',
        success:function (res) {
            min = res;
            //res便是的数据便是后端拿到的数据
            //这里需要注意：res为局部变量，
            //所以需要在方法外定义一个变量把res赋值给他，才能在方法之外使用。
        },
    })
}
function draw_line(){
    echarts();
    // 基于准备好的dom，初始化echarts实例
    var myChart = echarts.init(document.getElementById('line'));

    // 指定图表的配置项和数据
    var option = {
        title: {
            text: 'Commits between release'
        },
        tooltip: {
            trigger: 'axis'   // axis   item   none三个值
        },
        legend: {

            data:['销量']
        },
        xAxis: {
            data: ["衬衫","羊毛衫","雪纺衫","裤子","高跟鞋","袜子"]
        },
        yAxis: {},
        series: [{
            name: '销量',
            type: 'line',
            data: [5, 20, 36, 10, 10, 20]
        }]
    };

    // 使用刚指定的配置项和数据显示图表。
    myChart.setOption(option);
}
function open_issue(){

}

function close_issue(){

}

function draw_pie(){
    echarts();

}

function average(){
    $.ajax({
        url:'/repo/issue/solveTime/avg',	//这是后端接口的url
        method:'get',
        success:function (res) {
            average = res;
            //res便是的数据便是后端拿到的数据
            //这里需要注意：res为局部变量，
            //所以需要在方法外定义一个变量把res赋值给他，才能在方法之外使用。
        },
    })
}

function get_scatter(){
    $.ajax({
        url:'/repo/issue/close',	//这是后端接口的url
        method:'get',
        success:function (res) {
            close = res;
        },
    })
}
function find_dev(){
    $.ajax({
        url:'/developersTop',	//这是后端接口的url
        method:'get',
        success:function (res) {
            developers = res;
            document.getElementById("1").setAttribute("src",developers[0]);
            document.getElementById("2").setAttribute("src",developers[1]);
            document.getElementById("3").setAttribute("src",developers[2]);
            document.getElementById("4").setAttribute("src",developers[3]);
        },
    })
}
function draw_br(){
    // 基于准备好的dom，初始化echarts实例
    var myChart = echarts.init(document.getElementById('br'));

    // 指定图表的配置项和数据
    var option = {
        title: {
            text: 'Developer commit time'
        },
        tooltip: {
            trigger: 'axis'   // axis   item   none三个值
        },
        legend: {

            data:['销量']
        },
        xAxis: {
            data: ["衬衫","羊毛衫","雪纺衫","裤子","高跟鞋","袜子"]
        },
        yAxis: {},
        series: [{
            name: '销量',
            type: 'bar',
            data: [5, 20, 36, 10, 10, 20]
        }]
    };

    // 使用刚指定的配置项和数据显示图表。
    myChart.setOption(option);
}

function draw_scatter(){
    // 基于准备好的dom，初始化echarts实例
    var myChart = echarts.init(document.getElementById('scatter'));
    // var data=[[1,2],[2,3]];
    average();
    get_scatter();
    min();
    max();
    // 指定图表的配置项和数据
    var option = {
        title: {
            text: 'Solve time of issues'
        },
        tooltip: {},
        xAxis: {
            name:'open-time',
            splitLine: {
                lineStyle: {
                    type: 'dashed'
                }
            }
        },
        yAxis: {
            name:'time',
            splitLine: {
                lineStyle: {
                    type: 'dashed'
                }
            }},
        visualMap: {
            show: false,
            pieces: [
                {
                    gt: 0,
                    lte: average,          //这儿设置基线上下颜色区分 基线下面为绿色
                    color: '#e9a616'
                }, {

                    gt: average,          //这儿设置基线上下颜色区分 基线上面为红色
                    color: '#e07b6b'
                }]
        },

        series: [{

            type: 'scatter',
            data: data,
            markLine: {
                symbol:['none','none'],
                silent: true,
                lineStyle: {
                    normal: {
                        color: '#e06437'                   // 这儿设置安全基线颜色
                    }
                },
                data: [{
                    yAxis: average
                }],

            },
            markLine: {
                symbol:['none','none'],
                silent: true,
                lineStyle: {
                    normal: {
                        color: '#e9a616'                   // 这儿设置安全基线颜色
                    }
                },
                data: [{
                    yAxis: min
                }],

            },
            markLine: {
                symbol:['none','none'],
                silent: true,
                lineStyle: {
                    normal: {
                        color: '#e9a616'                   // 这儿设置安全基线颜色
                    }
                },
                data: [{
                    yAxis:  max
                }],

            }
        }],

    };
    myChart.setOption(option);
}

