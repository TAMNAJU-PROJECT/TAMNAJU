const writeForm = document.getElementById('write');

writeForm.onsubmit = function (e) {
	e.preventDefault();

	const xhr = new XMLHttpRequest();
	const formData = new FormData();
	formData.append('title', writeForm['title'].value);
	formData.append('content', writeForm['content'].value);
	xhr.onreadystatechange = () => {
		if (xhr.readyState !== XMLHttpRequest.DONE) {
			return;
		}
		if (xhr.status < 200 || xhr.status >= 300) {
			return;
		}
		const responseObject = JSON.parse(xhr.response);
		switch (responseObject['result']) {
			case 'success':
				window.location.href = './list';
				break;

			case 'failure':
				window.location.href = '/';
				break;

			default:
				break;
		}
	};
	xhr.open('POST', './write');
	xhr.send(formData);
};
