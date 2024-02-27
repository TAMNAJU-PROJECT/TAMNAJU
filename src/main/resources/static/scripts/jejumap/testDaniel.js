const map = document.getElementById('map'); // <div id="map></div>

const table = document.getElementById('table');

const tbody = table.querySelector(':scope > tbody');

const option = {
    center: new kakao.maps.LatLng(33.48569, 126.50033),
    level: 8, //확대, 축소 레벨
};
map.instance = new kakao.maps.Map(map, option);

const info = document.getElementById('info');
const infoShort = document.getElementById('info-short');
let marker = null;

let today = new Date();
if (today.getHours() < 3) {
    today.setDate(today.getDate() - 1);
}

// let date1 = currentDate.setDate(currentDate.getDate() + 1);
// let date2 = currentDate.setDate(currentDate.getDate() + 2);

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

    let sn =
        Math.tan(Math.PI * 0.25 + slat2 * 0.5) /
        Math.tan(Math.PI * 0.25 + slat1 * 0.5);
    sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);
    let sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
    sf = (Math.pow(sf, sn) * Math.cos(slat1)) / sn;
    let ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
    ro = (re * sf) / Math.pow(ro, sn);
    let rs = {};
    if (code == 'toXY') {
        rs['lat'] = v1;
        rs['lng'] = v2;
        let ra = Math.tan(Math.PI * 0.25 + v1 * DEGRAD * 0.5);
        ra = (re * sf) / Math.pow(ra, sn);
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
        let alat = Math.pow((re * sf) / ra, 1.0 / sn);
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
    console.log('getWeatherInfo 안으로 들어옴');
    axios
        .get(
            `https://api.openweathermap.org/data/2.5/weather?lat=` +
            latitude +
            `&lon=` +
            longitude +
            `&appid=47eccf027772f4e4e0e2fe8a3cb91e64`
        )
        .then((res) => {
            const responseObject = res.data;
            const weatherImage = res.data.weather[0].icon;
            console.log(res.data.weather);
            console.log(res.data.main);
            // const items = responseObject['weather'];
            console.log(responseObject);

            let imageSrc = `https://openweathermap.org/img/wn/${weatherImage}@2x.png`, // 마커이미지의 주소입니다
                imageSize = new kakao.maps.Size(64, 69), // 마커이미지의 크기입니다
                imageOption = { offset: new kakao.maps.Point(28, 32) }; // 마커이미지의 옵션입니다. 마커의 좌표와 일치시킬 이미지 안에서의 좌표를 설정합니다.

            // 마커의 이미지정보를 가지고 있는 마커이미지를 생성합니다
            let markerImage = new kakao.maps.MarkerImage(
                    imageSrc,
                    imageSize,
                    imageOption
                ),
                markerPosition = new kakao.maps.LatLng(latitude, longitude); // 마커가 표시될 위치입니다

            // 마커를 생성합니다
            if (marker !== null) {
                marker.setMap(null);
            }
            marker = new kakao.maps.Marker({
                position: markerPosition,
                image: markerImage, // 마커이미지 설정
                title:
                    Math.floor((res.data.main.temp - 273.15) * 100) / 100 + '℃',
            });

            marker.setMap(map.instance);

            info.show(res.data.weather[0], res.data.main);
        })
        .catch((err) => {
            console.log(err);
        });
}

function getShortWeatherInfo(modifiedDate, nx, ny) {
    console.log('getShortWeatherInfo 안으로 들어옴');
    axios
        .get(
            `https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst?serviceKey=hFAsHSPD%2BAwBzzeNDkOBJTRQMZrl%2B7He3Xb6y8RGldoSqs1MsVo9uTpcWvFw23oRbRMgUDEN3q%2FpHOocGTpopg%3D%3D&pageNo=1&numOfRows=1000&dataType=json&base_date=${modifiedDate}&base_time=0200&nx=${nx}&ny=${ny}`
        )
        .then((res) => {
            console.log(res);
            const items = res.data.response.body.items.item;
            console.log('===items===');
            console.log(items);
            console.log('===items===');
            // 일자별 data를 담기위한 빈 배열 생성
            const dayArray = [];
            for (let i = 0; i < 3; i++) {
                // 기준 일에 i를 추가한 깊은복사 객체 생성
                const copiedDate = new Date(today);
                const currentDate = new Date(
                    copiedDate.setDate(copiedDate.getDate() + i)
                );

                // 시간별로 분류하기 전, 일자별로 재단
                const datasPerDay = items.filter((data) => {
                    const year = currentDate.getFullYear();
                    const month = currentDate.getMonth() + 1; // 월은 0부터 시작하므로 +1 해줍니다.
                    const day = currentDate.getDate();
                    const modifiedDate =
                        year +
                        (month < 10 ? '0' : '') +
                        month +
                        (day < 10 ? '0' : '') +
                        day;
                    return data.fcstDate === modifiedDate;
                });
                // 시간별 data를 담기위한 빈 배열 생성
                const timeArray = [];
                for (let j = 0; j < 24; j++) {
                    // 일자별로 재단된 것을 시간별로 재단

                    const datasPerHour = datasPerDay.filter((time) => {
                        const formatTime = j < 10 ? `0${j}00` : `${j}00`;
                        return time.fcstTime === formatTime;
                    });
                    timeArray.push(datasPerHour);
                }
                dayArray.push(timeArray);
                console.log(timeArray);
            }

            // const weatherTime = weatherDay1.filter(time => {
            //     return time.fcstTime === `0${i}00`;
            // })
            console.log(dayArray);
            console.log('0000000000000000000000');

            const copiedDate = new Date(today);
            for (const timeArray of dayArray) {
                const date = document.createElement('td');
                date.innerText =
                    copiedDate.getFullYear() +
                    '-' +
                    copiedDate.getMonth() +
                    '-' +
                    copiedDate.getDate();
                date.rowSpan = 24;
                copiedDate.setDate(copiedDate.getDate() + 1);

                for (const datas of timeArray) {
                    const tr = document.createElement('tr');
                    const weather = document.createElement('td');
                    const temperature = document.createElement('td');
                    const type = document.createElement('td');
                    for (let i = 0; i < 24; i++) {
                        const category = datas[i]?.['category'];
                        switch (category) {
                            case 'TMP':
                                const time = datas[i]['fcstTime'] / 100;
                                const dividedTime =
                                    time % 12 === 0 ? 12 : time % 12;
                                weather.innerText =
                                    (time < 12 ? '오전 ' : '오후 ') +
                                    // (dividedTime < 10 ? '0' : '') +
                                    dividedTime +
                                    '시 기온 : ' +
                                    datas[i]['fcstValue'] +
                                    '˚C';
                                break;

                            case 'SKY':
                                let sky = '';
                                switch (datas[i]['fcstValue']) {
                                    case '1':
                                        sky = '맑음';
                                        break;

                                    case '3':
                                        sky = '구름 많음';
                                        break;

                                    case '4':
                                        sky = `흐림`;
                                        break;

                                    default:
                                        break;
                                }
                                temperature.innerText = sky;
                                break;

                            case 'PTY':
                                let pty = '';
                                switch (datas[i]['fcstValue']) {
                                    case '0':
                                        pty = '맑음';
                                        break;

                                    case '1':
                                        pty = '비';
                                        break;

                                    case '2':
                                        pty = '비/눈';
                                        break;

                                    case '3':
                                        pty = '눈';
                                        break;

                                    case '4':
                                        pty = '소나기';
                                        break;

                                    default:
                                        break;
                                }
                                type.innerText = '강수 형태 : ' + pty;
                                break;

                            default:
                                break;
                        }
                        if (i === 0) {
                            tr.append(date, weather, temperature, type);
                        } else {
                            tr.append(weather, temperature, type);
                        }
                    }
                    tbody.append(tr);
                }
            }

            // infoShort.show();
        })
        .catch((err) => {
            console.log('오류가 발생했습니다.');
            console.log(err);
        });
}

function getLongWeatherInfo(today, nx, ny) {
    console.log('getLongWeatherInfo 안으로 들어옴');
    axios.get();
}

info.querySelector('[rel="close"]').onclick = function () {
    info.hide();
};

info.show = (weather, main) => {
    info.querySelector(
        '[rel="icon"]'
    ).innerHTML = `<img src="https://openweathermap.org/img/wn/${weather['icon']}@2x.png" alt="weather_icon">`;
    console.log('item', weather);
    info.querySelector('[rel="temperature"]').innerText =
        Math.floor((main['temp'] - 273.15) * 100) / 100;
    info.querySelector('[rel="sky"]').innerText = weather['main'];
    info.querySelector('[rel="rain-style"]').innerText = weather['id'];
    // info.querySelector('[rel="rain-probability"]').innerText = item['강수확률'];
    info.querySelector('[rel="description"]').innerText =
        weather['description'];

    info.classList.add('visible');
};

info.hide = () => info.classList.remove('visible');
info.isVisible = () => info.classList.contains('visible');

// infoShort.show = (weather) => {}

kakao.maps.event.addListener(map.instance, 'click', function (position) {
    let clickedLatLng = position.latLng;

    getWeatherInfo(clickedLatLng.getLat(), clickedLatLng.getLng());
    let rs = dfs_xy_conv(
        'toXY',
        clickedLatLng.getLat(),
        clickedLatLng.getLng()
    );

    const year = today.getFullYear();
    const month = today.getMonth() + 1; // 월은 0부터 시작하므로 +1 해줍니다.
    const day = today.getDate();
    const modifiedDate =
        year + (month < 10 ? '0' : '') + month + (day < 10 ? '0' : '') + day;

    getShortWeatherInfo(modifiedDate, rs.x, rs.y);
});
