document.addEventListener('DOMContentLoaded', function () {
    const cards = document.querySelectorAll('.card');
    cards.forEach((card, index) => {
        card.style.animation = `floatUp 0.8s ease ${index * 0.08}s both`;
    });
});

window.addEventListener('scroll', function () {
    const hero = document.querySelector('.hero');
    if (hero) {
        hero.style.backgroundPosition = `center ${window.scrollY * 0.15}px`;
    }
});
