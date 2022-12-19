export function formatDate(date_time) {
    // var now = new Date(date_time)
    var now = new Date(parseInt(date_time));// 时间戳为10位需*1000，时间戳为13位的话不需乘1000
    var year=now.getFullYear();
    var month=now.getMonth()+1;
    if(month<10){
        month="0"+month;
    }
    var date=now.getDate();
    if(date<10){
        date="0"+date;
    }
    var hour=now.getHours();
    if(hour<10){
        hour="0"+hour;
    }
    var minute=now.getMinutes();
    if(minute<10){
        minute="0"+minute;
    }
    var second=now.getSeconds();
    if(second<10){
        second="0"+second;
    }
    return year+"-"+month+"-"+date+" "+hour+":"+minute+":"+second;
}


export function formatCtsDate(strDate) {
    if(null==strDate || ""==strDate){
        return "";
    }
    var dateStr=strDate.trim().split(" ");
    var strGMT = dateStr[0]+" "+dateStr[1]+" "+dateStr[2]+" "+dateStr[5]+" "+dateStr[3]+" GMT+0800";
    var date = new Date(Date.parse(strGMT));
    var y = date.getFullYear();
    var m = date.getMonth() + 1;
    m = m < 10 ? ('0' + m) : m;
    var d = date.getDate();
    d = d < 10 ? ('0' + d) : d;
    var h = date.getHours();
    var minute = date.getMinutes();
    minute = minute < 10 ? ('0' + minute) : minute;
    var second = date.getSeconds();
    second = second < 10 ? ('0' + second) : second;
 
    return y+"-"+m+"-"+d+" "+h+":"+minute+":"+second;
};