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

			const date = document.createElement('tr');
			const time = document.createElement('tr');
			const temperature = document.createElement('tr');
			const cloud = document.createElement('tr');
			const rain = document.createElement('tr');
			const rate = document.createElement('tr');

			for (const timeArray of dayArray) {
				const dateCell = document.createElement('td');
				const firstIndex = timeArray.findIndex(
					(datas) => datas[0]?.['fcstDate']
				);
				const dateText = timeArray[firstIndex][0]['fcstDate'];
				dateCell.innerText = `${dateText.substring(0, 4)}년 ${
					dateText.substring(4, 6) < 10
						? dateText.substring(5, 6)
						: dateText.substring(4, 6)
				}월 ${
					dateText.substring(6) < 10
						? dateText.substring(7)
						: dateText.substring(6)
				}일`;
				dateCell.colSpan = 24 - firstIndex;
				date.append(dateCell);
				for (const datas of timeArray) {
					if (datas[0]) {
						const timeCell = document.createElement('td');
						const hour24 = datas[0]['fcstTime'].substring(0, 2);
						const term = hour24 < 12 ? '오전' : '오후';
						const hour12 =
							hour24 % 12 === 0 ? '12' : `${hour24 % 12}`;
						timeCell.innerText = `${term}\n${hour12}시`;
						time.append(timeCell);
						for (const data of datas) {
							const cell = document.createElement('td');
							switch (data['category']) {
								case 'TMP':
									cell.innerText = data['fcstValue'] + '˚C';
									temperature.append(cell);
									break;

								case 'SKY':
									let sky = document.createElement('img');
									sky.classList.add('icon_cloud');
									sky.setAttribute('alt', 'weather_icon');
									switch (data['fcstValue']) {
										case '1': // 맑음
											sky.setAttribute(
												'src',
												'https://openweathermap.org/img/wn/01d@2x.png'
											);
											break;

										case '3': // 구름 많음
											sky.setAttribute(
												'src',
												'https://openweathermap.org/img/wn/03d@2x.png'
											);
											break;

										case '4': // 흐림
											sky.setAttribute(
												'src',
												'https://openweathermap.org/img/wn/04d@2x.png'
											);
											break;

										default:
											break;
									}
									cell.append(sky);
									cloud.append(cell);
									break;

								case 'PTY':
									let pty = document.createElement('img');
									pty.classList.add('icon_rain');
									pty.setAttribute('alt', 'rain_style');
									switch (data['fcstValue']) {
										case '0':
											pty.setAttribute(
												'src',
												'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAQSURBVHgBAQUA+v8A/////wn7A/2j0UkKAAAAAElFTkSuQmCC'
											);
											break;

										case '1':
											pty.setAttribute(
												'src',
												'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAYAAACqaXHeAAAABHNCSVQICAgIfAhkiAAAAAlwSFlzAAADAgAAAwIBpjVx6QAAABl0RVh0U29mdHdhcmUAd3d3Lmlua3NjYXBlLm9yZ5vuPBoAAAYLSURBVHic1ZttiFVFGMd/z77orpoorbZquSqbZaVFlGWGkegHQ6mIJIooTYossPrSh7QSoagwwl4QojeEMD9kVFRYYUiFyRpFb+ZLZdgWpa1vRequTx/mLM6dnXvvnHPnnrv7wIF75s78n//zP8/MnTMzV1SVWpiIzAReAA4Bz6nqhprwqIUAIjId+ByoT4p6gCtUdVvuXPIWQEQGA18C5zlffQ9crKrH8uRTl6ezxFbSN3iSspU5c8k3Azyp71ruXSE3AUqkvmu5doU8u4Av9b9NLtty7Qq5ZECR1D8BXJZ8/gJotL7LryuoalUvoAmT1upcK6w6Kzzf/wA0VZ1fDgI86QmuA2iw6jQkZW69pwa0AMDlmHS2gzoGXOCpexGmW9h1e7vCwBMgSf0dnqf6UIk2T3jq7wSaB6IAq8ulvqdNM7Db0+6ZASUAMLNI6k8NaDvHI8BJYNaAECB5ijs9QSxPgfGqp/0eYOhAEMCX+ttLpb4H43TgTw/Omn4tADDNM5IHpb4H62aPAN2YaXL/EwAQ4FMP6UcqwHzPg7eVZAbb3wRY5CH7IzC4Asw24KgH985+JQAwskifnRMB+z4P7gGgpT8JsNZD8vVI2HXANg/+S/1CAOBSz2/+QaA1BsHEx4WewfUkEabJMZ6O7yXm3ljBW74e9/j5CqivpQBLPaQ6gLoqCNAE7PL4W1YTATCTlS6HTA9wSezgLZ9XewQ4BIzKilnJktgyYIRTtlZVOyrALGmquhl42SkeDtyfFTPTkpiIDAf2UijAEWCCqv6dlUyg7xbgZ2CYVXwYaFPVg2nxsmbAUvo+/TXVDh5AVfcDzzvFw4F7suClzgARaQZ+AUZbxbk8fYtDS8JhqFW8H5MF/6bBypIBSygMHuDZvIKHolnQAtyVFitVBohII+a9/Cyr+AgwUVUPpHVeiYnIKMxYYGfBb8AkVT0eipM2A26lMHgwW9u5Bg+gqn9httdtGwfcngYnOANEpA6zyHm2VXwU0/dzFyDhNAozFgyxin8CJqtqTwhGmgy4jsLgoUZPv9eKZMEk4Po0IKGzsPUUzsBOAKOrNetLwasVs1Jkc1sfdSoMDMJMOW0nH9Q6eIvfJofbYQIXYkK7wGzMZMO2mpzpKWJvOPenYZbXy1qoAPOc+xPAxsC2edibGE62XRPSMFSAyc79DlXtCmxbdUu47HCK20PahgrQ5tzvCmwX1USkTkTmishyEXHnIy6nCSGYWQXYHdguti3BDHirgN0iMsH6zuU0XkSkHGBZARKQRqe4u1y7KtkD1udBwI3WvTvxqdOAWV5ZARKQP5ziseXaxTYRGQGc4xTbA9+Zzne/h+CGdoFO5z53AYCpnrLvrM+uAC5nr2UVwHWWh832lNkCuONUVAH2OfdTRKQ1sG3FJiJNmFUo2w6oamfyfRvmHcC2vSHYoQJ87HIC5ge2jWGL6LsIYy+OLvC02RSEHDjXbgb+oXC+/XZO8/zJ9H0POQ6MK/EucAgYFO1lKHGy0XHyn02iSsEPw/Rzdy9gnVVnYiJI9d4GE0e3eYi8WMXgpwBfe3weBNqteus8dW6qhgBDMSOr7agbOLcKwd/i6XK9G6ILrHrT6Lsxuyc0/VMJkDhc4iG1iYh7gcAMT0r3Xo9a9RqATzx1Fqbyl5JcPfCNx+nqiAKsKhL8aqyjMcAaT52tqf1lIDivCMHFkQRwD0d1Adc6de4o0j2urLoAJdQ/TqSzO8BiYAvwGmbV2Q3e10UezuQrI8F64MMimbAWaIwhhMfn00V8biDjybFKCI3EfyJUMUfZpkcM/nzgoyK+tgNDMmNXSKy9hAi9T6a9AvzxwCv0/anrvTqAMRXFEOHpjADeLyFCN7AZc4hhUgDeOODuBPNYCdz1RDhGH+U/Q8m22WPAgwHV92E2MTs5NbFqBc4AxlB+MVMxp09XZSZcgBapnyZCXoX/TF+s6zNgRlTOMcESEQRYiP+PD1mvXcANsblG6wI+S84SLMBsqs7H/GqksS7gXeAt4B1VdTc+olhe/xtsAGYBczEj+1jrglPjQSfwK2aOsUVVq776/D+H5+1RhiNuxgAAAABJRU5ErkJggg=='
											);
											break;

										case '2':
											pty.setAttribute(
												'src',
												'data:image/png;base64,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'
											);
											break;

										case '3':
											pty.setAttribute(
												'src',
												'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAMAAACdt4HsAAAAA3NCSVQICAjb4U/gAAAACXBIWXMAAAQIAAAECAE9qZIOAAAAGXRFWHRTb2Z0d2FyZQB3d3cuaW5rc2NhcGUub3Jnm+48GgAAAZVQTFRF////AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAxy7YmgAAAIZ0Uk5TAAEDBAUGBwgJCgsMDxMUFRYXGBkdHyAhIiMkJScoKSorLC0uLzAxMjM0Nj1AQkRFRkdISUtOUFFZW11eY2lqbXBzdoKFiIqLjJOUlZqbnJ2en6Ckqqytr7Gytrq8wMTFyMnLzc7Q0tPU1dfb3N7i4+Tl5ujp6uzu7/Dx8vT19vf4+fv8/f5FDJVHAAACwklEQVRYw2NgwAuMk5KMGSgATHltbXlMFBig3AYEyhQYoAoyQHVIGsDn72OGaoCZjz8fCQakAvXFqiEMUIsF0qnE62duAmlsCRSGGCAc2AKim5iJNyG4DQxqgkBkUA2EF0yMTjF3D1EgxenV1IYGmrw4gRKiHq4ieA1IbGtLATPkolD1R8mBhVPa2uLw6RcFqZWGsA0zENozDCFi0iCOKB4D5JGTLpNtKUR7qS0TctKWx6KREUorgRQowoV5vEF8bx64gCKIr4SmCQjUc0tDJEAMFTQb0FMi2IUqIJZESGmuOlw8ASjc4MIFTDAgBbK4DZAF8YEJjMulAchIgIvngL1aYMmoCaJlcBsgA+JrMloWgDXkwMWNWiChFW+HFAvYDADHgl08RHWLEUJCKwEpyiVxGyCJpCxBCyUarIvgMuK4DRCHKyqyZkSLSW7XRqicNW4DrKFKGl25sSQGqWSodIwidgMUY6AKkqWwpUJ+vxaY+5q9eTEN4PVuhsm3+PJjFgEOlch5p9yeGdUAZvtyZPkKe7TCXi8TIlHv7AnNyJn6yAboQ+WbPJ3rIaw0HWT9NlCDw8QYGBSioZwIKZgBUhFQoWgFYEyEQzlWSBVQLcRSPQjXNBsa1m7aIErbDRo/2aYQeYMsMLcWyReFQH6lA7zMY3WqhmiphRNtbdVOrDB5FscqoEAhkhesa+p8BZD9JBjQiloitQYIIssLBdbXWCMLsLCjR4tGHLL+OA10eQ4WggWsRT5Me74FWfUaSQZgtgEIeQFVi0lZCYmBGFBaZsJAvWikPCFRmpQpz0yUZ2fKCxTKizQKC1VKi3VKKxaKqzaKK1eKq3fd4sZQUhsYoY3FukiRwEZ6E4eNkZqNLKo184hpaCZS2NR1F6NtY5tazf10bB2OdBLqFJmQSHPUhGQeGSIzsrp9VOv6Utz5JqL7DwBgU5rQG//cTgAAAABJRU5ErkJggg=='
											);
											break;

										case '4':
											pty.setAttribute(
												'src',
												'data:image/png;base64,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'
											);
											break;

										default:
											break;
									}
									cell.append(pty);
									rain.append(cell);
									break;

								case 'POP':
									cell.innerText = data['fcstValue'] + '%';
									rate.append(cell);
									break;

								default:
									break;
							}
						}
					}
				}
			}
			tbody.append(date, time, temperature, cloud, rain, rate);
			info.classList.add('visible');
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
};

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

			const date = document.createElement('tr');
			const time = document.createElement('tr');
			const temperature = document.createElement('tr');
			const cloud = document.createElement('tr');
			const rain = document.createElement('tr');

			for (const timeArray of dayArray) {
				const dateCell = document.createElement('td');
				const firstIndex = timeArray.findIndex(
					(datas) => datas[0]?.['fcstDate']
				);
				const dateText = timeArray[firstIndex][0]['fcstDate'];
				dateCell.innerText = `${dateText.substring(0, 4)}년 ${
					dateText.substring(4, 6) < 10
						? dateText.substring(5, 6)
						: dateText.substring(4, 6)
				}월 ${
					dateText.substring(6) < 10
						? dateText.substring(7)
						: dateText.substring(6)
				}일`;
				dateCell.colSpan = 24 - firstIndex;
				date.append(dateCell);
				for (const datas of timeArray) {
					if (datas[0]) {
						const timeCell = document.createElement('td');
						const hour24 = datas[0]['fcstTime'].substring(0, 2);
						const term = hour24 < 12 ? '오전' : '오후';
						const hour12 = hour24 % 12 === 0 ? 12 : hour24;
						timeCell.innerText = `${term}\n${
							hour12 < 10 ? hour12.substring(1) : hour12
						}시`;
						time.append(timeCell);
						for (const data of datas) {
							const cell = document.createElement('td');
							switch (data['category']) {
								case 'TMP':
									cell.innerText = data['fcstValue'] + '˚C';
									temperature.append(cell);
									break;

								case 'SKY':
									let sky = '';
									switch (data['fcstValue']) {
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
									cell.innerText = sky;
									cloud.append(cell);
									break;

								case 'PTY':
									let pty = '';
									switch (data['fcstValue']) {
										case '0':
											pty = '없음';
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
									cell.innerText = pty;
									rain.append(cell);
									break;

								default:
									break;
							}
						}
					}
				}
			}
			tbody.append(date, time, temperature, cloud, rain);
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

info.hide = () => info.classList.remove('visible');
info.isVisible = () => info.classList.contains('visible');

// infoShort.show = (weather) => {}

kakao.maps.event.addListener(map.instance, 'click', function (position) {
	// 이벤트가 반복될 시, 데이터가 누적되는 것을 방지
	tbody.innerHTML = '';
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
