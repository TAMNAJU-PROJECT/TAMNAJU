const map = document.getElementById('map'); // <div id="map></div>

const option = {
    center: new kakao.maps.LatLng(33.48569, 126.50033),
    level: 8 //확대, 축소 레벨
};
map.instance = new kakao.maps.Map(map, option);
console.log(kakao.maps.event);
console.log(kakao.maps.event.addListener);

kakao.maps.event.addListener(map,'click', function (mouseEvent) {
    console.log(mouseEvent);
    console.log(typeof mouseEvent);
    let clickedLatLng = mouseEvent.latLng;
    console.log('hello', clickedLatLng);
    console.log('Clicked Longitude:', clickedLatLng.getLng());
    console.log('Clicked Latitude:', clickedLatLng.getLat());

    let coords = new kakao.maps.LatLng(clickedLatLng.getLat(), clickedLatLng.getLng());
    let wgs84Coords = kakao.maps.CoordType.UTMK.fromLatLngToWGS84(coords);

    getWeatherInfo(wgs84Coords.getLat(), wgs84Coords.getLng());
});

function getWeatherInfo(latitude, longitude) {

    axios.get(`https://api.openweathermap.org/data/2.5/weather?lat=${latitude}&lon=${longitude}&appid=47eccf027772f4e4e0e2fe8a3cb91e64`)
        .then(res =>{
            console.log("잘 작동 된다.");
        })
        .catch(err=>{console.log(err);});

}


const cover = document.getElementById('cover');

cover.show = () => cover.classList.add('visible');
cover.hide = () => cover.classList.remove('visible');
cover.isVisible = () => cover.classList.contains('visible');

const info = document.getElementById('info');

info.querySelector('[rel="close"]').onclick = function () {
    cover.hide();
    info.hide();
}

info.show = (item) => {
    info.querySelector('[rel="temperature"]').innerText = item['introduction'];
    info.querySelector('[rel="sky"]').innerText = item['phoneno'];
    info.querySelector('[rel="rain-probability"]').innerText = item[''];
    info.querySelector('[rel="rain-style"]').innerText = item[''];


    info.classList.add('visible');
};
info.hide = () => info.classList.remove('visible');
info.isVisible = () => info.classList.contains('visible');