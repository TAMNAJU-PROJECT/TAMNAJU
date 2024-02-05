const map = document.getElementById('map'); // <div id="map></div>

const option = {
    center: new kakao.maps.LatLng(33.48569, 126.50033),
    level: 8 //확대, 축소 레벨
};
map.instance = new kakao.maps.Map(map, option);

const cover = document.getElementById('cover');

cover.show = () => cover.classList.add('visible');
cover.hide = () => cover.classList.remove('visible');
cover.isVisible = () => cover.classList.contains('visible');

const info = document.getElementById('info');
const infoShort = document.getElementById('info-short');


// 소스출처 : http://www.kma.go.kr/weather/forecast/digital_forecast.jsp  내부에 있음
// 기상청에서 이걸 왜 공식적으로 공개하지 않을까?
//
// (사용 예)
//let rs = dfs_xy_conv("toLL","60","127");
// console.log(rs.lat, rs.lng);
//
// LCC DFS 좌표변환을 위한 기초 자료
//
const RE = 6371.00877; // 지구 반경(km)
const GRID = 5.0; // 격자 간격(km)
const SLAT1 = 30.0; // 투영 위도1(degree)
const SLAT2 = 60.0; // 투영 위도2(degree)
const OLON = 126.0; // 기준점 경도(degree)
const OLAT = 38.0; // 기준점 위도(degree)
const XO = 43; // 기준점 X좌표(GRID)
const YO = 136; // 기1준점 Y좌표(GRID)
//
// LCC DFS 좌표변환 ( code : "toXY"(위경도->좌표, v1:위도, v2:경도), "toLL"(좌표->위경도,v1:x, v2:y) )
//

function dfs_xy_conv(code, v1, v2) {
    let DEGRAD = Math.PI / 180.0;
    let RADDEG = 180.0 / Math.PI;

    let re = RE / GRID;
    let slat1 = SLAT1 * DEGRAD;
    let slat2 = SLAT2 * DEGRAD;
    let olon = OLON * DEGRAD;
    let olat = OLAT * DEGRAD;

    let sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
    sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);
    let sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
    sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;
    let ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
    ro = re * sf / Math.pow(ro, sn);
    let rs = {};
    if (code == "toXY") {
        rs['lat'] = v1;
        rs['lng'] = v2;
        let ra = Math.tan(Math.PI * 0.25 + (v1) * DEGRAD * 0.5);
        ra = re * sf / Math.pow(ra, sn);
        let theta = v2 * DEGRAD - olon;
        if (theta > Math.PI) theta -= 2.0 * Math.PI;
        if (theta < -Math.PI) theta += 2.0 * Math.PI;
        theta *= sn;
        rs['x'] = Math.floor(ra * Math.sin(theta) + XO + 0.5);
        rs['y'] = Math.floor(ro - ra * Math.cos(theta) + YO + 0.5);
    } else {
        rs['x'] = v1;
        rs['y'] = v2;
        let xn = v1 - XO;
        let yn = ro - v2 + YO;
        ra = Math.sqrt(xn * xn + yn * yn);
        if (sn < 0.0) -ra;
        let alat = Math.pow((re * sf / ra), (1.0 / sn));
        alat = 2.0 * Math.atan(alat) - Math.PI * 0.5;

        if (Math.abs(xn) <= 0.0) {
            theta = 0.0;
        } else {
            if (Math.abs(yn) <= 0.0) {
                theta = Math.PI * 0.5;
                if (xn < 0.0) -theta;
            } else theta = Math.atan2(xn, yn);
        }
        let alon = theta / sn + olon;
        rs['lat'] = alat * RADDEG;
        rs['lng'] = alon * RADDEG;
    }
    return rs;
}

function getWeatherInfo(latitude, longitude) {
    console.log("getWeatherInfo 안으로 들어옴");
    axios.get(`https://api.openweathermap.org/data/2.5/weather?lat=` + latitude + `&lon=` + longitude + `&appid=47eccf027772f4e4e0e2fe8a3cb91e64`)
        .then(res => {
            const responseObject = res.data;
            const weatherImage = res.data.weather[0].icon;
            console.log(res.data.weather);
            console.log(res.data.main);
            // const items = responseObject['weather'];
            console.log(responseObject);


            let imageSrc = `https://openweathermap.org/img/wn/${weatherImage}@2x.png`, // 마커이미지의 주소입니다
                imageSize = new kakao.maps.Size(64, 69), // 마커이미지의 크기입니다
                imageOption = {offset: new kakao.maps.Point(28, 32)}; // 마커이미지의 옵션입니다. 마커의 좌표와 일치시킬 이미지 안에서의 좌표를 설정합니다.

// 마커의 이미지정보를 가지고 있는 마커이미지를 생성합니다
            let markerImage = new kakao.maps.MarkerImage(imageSrc, imageSize, imageOption),
                markerPosition = new kakao.maps.LatLng(latitude, longitude); // 마커가 표시될 위치입니다

// 마커를 생성합니다
            let marker = new kakao.maps.Marker({
                position: markerPosition,
                image: markerImage, // 마커이미지 설정
                title: Math.floor((res.data.main.temp - 273.15) * 100) / 100
            });

// 마커가 지도 위에 표시되도록 설정합니다
            marker.setMap(map.instance);

            info.show(res.data.weather[0], res.data.main);

        })
        .catch(err => {
            console.log(err);
        });

}

function getShortWeatherInfo(today, nx, ny) {
    console.log("getShortWeatherInfo 안으로 들어옴");
    axios.get(`https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst?serviceKey=hFAsHSPD%2BAwBzzeNDkOBJTRQMZrl%2B7He3Xb6y8RGldoSqs1MsVo9uTpcWvFw23oRbRMgUDEN3q%2FpHOocGTpopg%3D%3D&pageNo=1&numOfRows=100&dataType=json&base_date=${today}&base_time=0500&nx=${nx}&ny=${ny}`)
        .then(res => {
            console.log(res);
            const items = res.data.response.body.items.item;

            const hourWeather1 = items.filter(weather => {
                return weather.fcstTime === "0700" && (weather.category ==="TMP" || weather.category ==="SKY" || weather.category ==="PTY" || weather.category ==="POP");
            });
            const hourWeather2 = items.filter(weather => {
                return weather.fcstTime === "0800" && (weather.category ==="TMP" || weather.category ==="SKY" || weather.category ==="PTY" || weather.category ==="POP");
            });
            for(const item of items) {
                infoShort.show(item);
            }

            console.log(hourWeather1);
            console.log(hourWeather2);
            infoShort.show();
        })
        .catch(err => {
            console.log(err);
        });
}

function getLongWeatherInfo(today, nx, ny) {
    console.log("getLongWeatherInfo 안으로 들어옴");
    axios.get();
}


info.querySelector('[rel="close"]').onclick = function () {
    cover.hide();
    info.hide();
}

info.show = (weather, main) => {
    info.querySelector('[rel="icon"]').innerHTML = `<img src="https://openweathermap.org/img/wn/${weather['icon']}@2x.png" alt="weather_icon">`;
    console.log('item', weather);
    info.querySelector('[rel="temperature"]').innerText = Math.floor((main['temp'] - 273.15) * 100) / 100;
    info.querySelector('[rel="sky"]').innerText = weather['main'];
    info.querySelector('[rel="rain-style"]').innerText = weather['id'];
    // info.querySelector('[rel="rain-probability"]').innerText = item['강수확률'];
    info.querySelector('[rel="description"]').innerText = weather['description'];

    info.classList.add('visible');
};

info.hide = () => info.classList.remove('visible');
info.isVisible = () => info.classList.contains('visible');


infoShort.show = (weather) => {
    infoShort.querySelector('[rel="icon"]').innerHTML = weather;
    infoShort.querySelector('[rel="temperature"]').innerText = weather;
}

kakao.maps.event.addListener(map.instance, 'click', function (position) {
    console.log(position);
    console.log(typeof position);
    let clickedLatLng = position.latLng;
    console.log('Clicked Longitude:', clickedLatLng.getLng());
    console.log('Clicked Latitude:', clickedLatLng.getLat());
    console.log(kakao.maps.services.Coords);

    getWeatherInfo(clickedLatLng.getLat(), clickedLatLng.getLng());
    let rs = dfs_xy_conv("toXY", clickedLatLng.getLat(), clickedLatLng.getLng());
    const currentDate = new Date();
    const hour = currentDate.getHours();

    if (hour >= 0 && hour < 5) { // 기상청에서 매일 12시가 아니라 새벽5시에 정보 갱신하기 때문.
        const yesterDate = currentDate.setDate(currentDate.getDate() - 1);
        const year = yesterDate.getFullYear();
        const month = yesterDate.getMonth() + 1;
        const day = yesterDate.getDate();
        let today = year + (month < 10 ? '0' : '') + month + (day < 10 ? '0' : '') + day;
        getShortWeatherInfo(today, rs.x, rs.y);
    } else {
        const year = currentDate.getFullYear();
        const month = currentDate.getMonth() + 1; // 월은 0부터 시작하므로 +1 해줍니다.
        const day = currentDate.getDate();
        let today = year + (month < 10 ? '0' : '') + month + (day < 10 ? '0' : '') + day;
        getShortWeatherInfo(today, rs.x, rs.y);
    }
});
