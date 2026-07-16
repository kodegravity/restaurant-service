-- Insert default cuisines
INSERT INTO restaurant_service.cuisine (id, name, description, active) VALUES
    (gen_random_uuid(), 'Italian', 'Italian cuisine including pasta, pizza, and Mediterranean dishes', true),
    (gen_random_uuid(), 'Chinese', 'Traditional and modern Chinese cuisine', true),
    (gen_random_uuid(), 'Indian', 'Indian cuisine with diverse regional flavors', true),
    (gen_random_uuid(), 'Japanese', 'Japanese cuisine including sushi, ramen, and traditional dishes', true),
    (gen_random_uuid(), 'Mexican', 'Mexican cuisine with tacos, burritos, and traditional flavors', true),
    (gen_random_uuid(), 'Thai', 'Thai cuisine with aromatic and spicy flavors', true),
    (gen_random_uuid(), 'French', 'Classic French cuisine and pastries', true),
    (gen_random_uuid(), 'American', 'American cuisine including burgers, BBQ, and comfort food', true),
    (gen_random_uuid(), 'Korean', 'Korean cuisine with BBQ, kimchi, and traditional dishes', true),
    (gen_random_uuid(), 'Mediterranean', 'Mediterranean cuisine with fresh and healthy options', true),
    (gen_random_uuid(), 'Greek', 'Greek cuisine with fresh ingredients and traditional flavors', true),
    (gen_random_uuid(), 'Vietnamese', 'Vietnamese cuisine with pho, spring rolls, and fresh herbs', true),
    (gen_random_uuid(), 'Middle Eastern', 'Middle Eastern cuisine with kebabs, falafel, and hummus', true),
    (gen_random_uuid(), 'Spanish', 'Spanish cuisine with tapas, paella, and regional specialties', true),
    (gen_random_uuid(), 'Caribbean', 'Caribbean cuisine with tropical flavors and jerk spices', true);
