rootProject.name = "E-Commerce-Microservice-Web"

include (
    ":Common",
    ":product-service",
    ":service-discovery",
    ":api-gateway",
    ":auth-service",
    ":user-service",
    ":order-service",
    ":notification-service",
    ":payment-service"
)