(function () {
    function initHeaderNav() {
        const header = document.querySelector(".nav-header");
        const hamburger = document.querySelector(".nav-hamburger");

        if (!header || !hamburger) {
            console.log("[Soundscape header] .nav-header or .nav-hamburger not found on this page");
            return;
        }

        console.log("[Soundscape header] init on", window.location.pathname);

        // Toggle nav-open on click
        hamburger.addEventListener("click", function (e) {
            e.stopPropagation(); // don't bubble to document
            header.classList.toggle("nav-open");
            console.log(
                "[Soundscape header] nav-open =",
                header.classList.contains("nav-open")
            );
        });

        // Close menu if you click outside the header
        document.addEventListener("click", function (e) {
            if (!header.contains(e.target)) {
                if (header.classList.contains("nav-open")) {
                    header.classList.remove("nav-open");
                    console.log("[Soundscape header] closed via outside click");
                }
            }
        });

        // Close menu when you click a mobile nav link
        const mobileLinks = header.querySelectorAll(".nav-mobile-menu a");
        mobileLinks.forEach(link => {
            link.addEventListener("click", () => {
                header.classList.remove("nav-open");
                console.log("[Soundscape header] closed via mobile link");
            });
        });
    }

    // Run immediately if DOM is ready; otherwise wait
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", initHeaderNav);
    } else {
        initHeaderNav();
    }
})();
