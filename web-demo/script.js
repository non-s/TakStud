// Navigation between screens
function goToTeacherDashboard() {
    document.getElementById('homeScreen').classList.remove('active');
    document.getElementById('teacherDashboard').classList.add('active');

    // Animate cards on entry
    const cards = document.querySelectorAll('.action-card');
    cards.forEach((card, index) => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px)';
        setTimeout(() => {
            card.style.transition = 'all 0.5s ease';
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';
        }, index * 100);
    });
}

function goToHome() {
    document.getElementById('teacherDashboard').classList.remove('active');
    document.getElementById('homeScreen').classList.add('active');
}

// Tab switching
function showTab(tabName) {
    const tabs = document.querySelectorAll('.tab');
    const analyticsContent = document.getElementById('analyticsContent');
    const actionCards = document.querySelector('.action-cards');

    tabs.forEach(tab => tab.classList.remove('active'));

    if (tabName === 'analytics') {
        tabs[1].classList.add('active');
        actionCards.style.display = 'none';
        analyticsContent.style.display = 'block';

        // Animate metrics
        const metrics = document.querySelectorAll('.metric-card');
        metrics.forEach((metric, index) => {
            metric.style.opacity = '0';
            metric.style.transform = 'scale(0.8)';
            setTimeout(() => {
                metric.style.transition = 'all 0.5s ease';
                metric.style.opacity = '1';
                metric.style.transform = 'scale(1)';
            }, index * 100);
        });
    } else {
        tabs[0].classList.add('active');
        actionCards.style.display = 'flex';
        analyticsContent.style.display = 'none';
    }
}

// Add click animations to action cards
document.addEventListener('DOMContentLoaded', () => {
    const actionCards = document.querySelectorAll('.action-card');

    actionCards.forEach(card => {
        card.addEventListener('click', function () {
            this.style.transform = 'scale(0.96)';
            setTimeout(() => {
                this.style.transform = '';
            }, 150);
        });
    });

    // Add ripple effect to buttons
    const buttons = document.querySelectorAll('.btn-gradient, .btn-outline');

    buttons.forEach(button => {
        button.addEventListener('click', function (e) {
            const ripple = document.createElement('span');
            const rect = this.getBoundingClientRect();
            const size = Math.max(rect.width, rect.height);
            const x = e.clientX - rect.left - size / 2;
            const y = e.clientY - rect.top - size / 2;

            ripple.style.width = ripple.style.height = size + 'px';
            ripple.style.left = x + 'px';
            ripple.style.top = y + 'px';
            ripple.classList.add('ripple');

            this.appendChild(ripple);

            setTimeout(() => ripple.remove(), 600);
        });
    });
});

// Add ripple CSS dynamically
const style = document.createElement('style');
style.textContent = `
    .btn-gradient, .btn-outline {
        position: relative;
        overflow: hidden;
    }
    
    .ripple {
        position: absolute;
        border-radius: 50%;
        background: rgba(255, 255, 255, 0.6);
        transform: scale(0);
        animation: ripple-animation 0.6s ease-out;
        pointer-events: none;
    }
    
    @keyframes ripple-animation {
        to {
            transform: scale(4);
            opacity: 0;
        }
    }
`;
document.head.appendChild(style);
