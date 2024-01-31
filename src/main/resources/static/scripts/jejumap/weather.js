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


function getWeatherInfo(latitude, longitude) {
    console.log("function 안으로 들어옴");
    axios.get(`https://api.openweathermap.org/data/2.5/weather?lat=`+ latitude +`&lon=`+ longitude +`&appid=47eccf027772f4e4e0e2fe8a3cb91e64`)
        .then(res =>{
            const responseObject = res.data;
            console.log(res.data.weather);
            console.log(res.data.main);
            // const items = responseObject['weather'];
            console.log(responseObject);

                info.show(res.data.weather[0],res.data.main);




            console.log("잘 작동 된다.");


        })
        .catch(err=>{console.log(err);});

}

info.querySelector('[rel="close"]').onclick = function () {
    cover.hide();
    info.hide();
}

info.show = (weather,main) => {
    info.querySelector('[rel="icon"]').innerHTML = `<img src="https://openweathermap.org/img/wn/${weather['icon']}@2x.png" alt="weather_icon">`;
    console.log('item',weather);
    info.querySelector('[rel="temperature"]').innerText = Math.floor((main['temp'] - 273.15)*100)/100;
    info.querySelector('[rel="sky"]').innerText = weather['main'];
    info.querySelector('[rel="rain-style"]').innerText = weather['id'];
    // info.querySelector('[rel="rain-probability"]').innerText = item['강수확률'];
    info.querySelector('[rel="description"]').innerText = weather['description'];

    info.classList.add('visible');
};

info.hide = () => info.classList.remove('visible');
info.isVisible = () => info.classList.contains('visible');



kakao.maps.event.addListener(map.instance,'click', function (position) {
    console.log(position);
    console.log(typeof position);
    let clickedLatLng = position.latLng;
    console.log('Clicked Longitude:', clickedLatLng.getLng());
    console.log('Clicked Latitude:', clickedLatLng.getLat());
    console.log(kakao.maps.services.Coords);

getWeatherInfo(clickedLatLng.getLat(), clickedLatLng.getLng());
});
